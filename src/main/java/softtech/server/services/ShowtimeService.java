package softtech.server.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import softtech.server.dto.BookingDTO.ShowtimeRequestDTO;
import softtech.server.dto.BookingDTO.ShowtimeResponseDTO;
import softtech.server.dto.ShowtimeDTO.RoomScheduleDTO;
import softtech.server.dto.ShowtimeDTO.ScheduleRequestDTO;
import softtech.server.models.Movie;
import softtech.server.models.Room;
import softtech.server.models.Seat;
import softtech.server.models.Showtime;
import softtech.server.repositories.MovieRepo;
import softtech.server.repositories.RoomRepo;
import softtech.server.repositories.SeatRepo;
import softtech.server.repositories.ShowtimeRepo;
import softtech.server.repositories.ShowtimeSeatRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepo showtimeRepo;
    private final MovieRepo movieRepo;
    private final RoomRepo roomRepo;
    private final SeatRepo seatRepo;

    private final Map<String, Double> revenueCache = new HashMap<>();

    // Khoảng cách giữa các suất
    private static final int MIN_GAP_MINUTES = 15;
    private static final int CLEANING_TIME = 15; // Thời gian dọn phòng

    // moi them ne
    private final ShowtimeSeatRepo showtimeSeatRepo;

    // Thời gian hoạt động rạp (đẹp & thực tế hơn)
    private static final LocalTime WEEKDAY_OPEN  = LocalTime.of(7, 0);
    private static final LocalTime WEEKDAY_CLOSE = LocalTime.of(22, 0);
    private static final LocalTime WEEKEND_OPEN  = LocalTime.of(8, 0);
    private static final LocalTime WEEKEND_CLOSE = LocalTime.of(21, 0);

    public List<ShowtimeResponseDTO> getAllShowtimesForNext14Days() {
        LocalDateTime now = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fourteenDaysLater = now.plusDays(14);

        List<Showtime> allShowtimes = showtimeRepo.findAll();
        if (allShowtimes == null || allShowtimes.isEmpty()) {
            return Collections.emptyList();
        }

        return allShowtimes.stream()
                .filter(st -> st.getStartTime() != null)
                .filter(st -> !st.getStartTime().isBefore(now) && st.getStartTime().isBefore(fourteenDaysLater))
                .sorted((st1, st2) -> st1.getStartTime().compareTo(st2.getStartTime()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ShowtimeResponseDTO> getShowtimesForNext7Days(String movieId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);

        List<Showtime> allShowtimes = showtimeRepo.findByMovie_MovieId(movieId);
        if (allShowtimes == null || allShowtimes.isEmpty()) {
            return Collections.emptyList();
        }

        return allShowtimes.stream()
                .filter(st -> st.getStartTime() != null)
                .filter(st -> !st.getStartTime().isBefore(now) && st.getStartTime().isBefore(sevenDaysLater))
                .sorted((st1, st2) -> st1.getStartTime().compareTo(st2.getStartTime()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ShowtimeResponseDTO> getShowtimesByDate(String movieId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<Showtime> allShowtimes = showtimeRepo.findByMovie_MovieId(movieId);
        if (allShowtimes == null || allShowtimes.isEmpty()) {
            return Collections.emptyList();
        }

        return allShowtimes.stream()
                .filter(st -> st.getStartTime() != null)
                .filter(st -> !st.getStartTime().isBefore(startOfDay) && !st.getStartTime().isAfter(endOfDay))
                .sorted((st1, st2) -> st1.getStartTime().compareTo(st2.getStartTime()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Showtime getShowtimeById(String showtimeId) {
        if (showtimeId == null || showtimeId.isEmpty()) return null;
        return showtimeRepo.findById(showtimeId).orElse(null);
    }

    public List<LocalDate> getAvailableDatesForNext7Days(String movieId) {
        List<Showtime> showtimes = showtimeRepo.findByMovie_MovieId(movieId)
                .stream()
                .filter(st -> {
                    if (st.getStartTime() == null) return false;
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime sevenDaysLater = now.plusDays(7);
                    return !st.getStartTime().isBefore(now) && st.getStartTime().isBefore(sevenDaysLater);
                })
                .collect(Collectors.toList());
        return showtimes.stream()
                .map(st -> st.getStartTime().toLocalDate())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getAvailableLanguages(String movieId) {
        if (movieId == null || movieId.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> langs = showtimeRepo.findDistinctLanguagesByMovieId(movieId);
        return langs != null ? langs : Collections.emptyList();
    }

    @Transactional
    public ShowtimeResponseDTO createShowtime(ShowtimeRequestDTO request) {
        validateShowtimeRequest(request);

        Movie movie = movieRepo.findById(request.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found with id: " + request.getMovieId()));

        Room room = roomRepo.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + request.getRoomId()));

        LocalDateTime startTime = LocalDateTime.parse(request.getStartTime());

        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());

        checkTimeConflict(request.getRoomId(), startTime, endTime, null);

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .room(room)
                .startTime(startTime)
                .endTime(endTime)
                .language(request.getLanguage())
                .build();

        Showtime savedShowtime = showtimeRepo.save(showtime);
        return convertToDTO(savedShowtime);
    }

    @Transactional
    public ShowtimeResponseDTO updateShowtime(String showtimeId, ShowtimeRequestDTO request) {
        Showtime showtime = showtimeRepo.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found with id: " + showtimeId));

        boolean needsConflictCheck = false;
        LocalDateTime newStartTime = showtime.getStartTime();
        LocalDateTime newEndTime = showtime.getEndTime();
        String newRoomId = showtime.getRoom().getRoomId();

        if (request.getMovieId() != null && !request.getMovieId().equals(showtime.getMovie().getMovieId())) {
            Movie movie = movieRepo.findById(request.getMovieId())
                    .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
            showtime.setMovie(movie);
            needsConflictCheck = true;

            if (request.getStartTime() != null) {
                newStartTime = LocalDateTime.parse(request.getStartTime());
            }
            newEndTime = newStartTime.plusMinutes(movie.getDuration());
        }

        if (request.getRoomId() != null && !request.getRoomId().equals(showtime.getRoom().getRoomId())) {
            Room room = roomRepo.findById(request.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Room not found"));
            showtime.setRoom(room);
            newRoomId = request.getRoomId();
            needsConflictCheck = true;
        }

        if (request.getStartTime() != null) {
            newStartTime = LocalDateTime.parse(request.getStartTime());
            newEndTime = newStartTime.plusMinutes(showtime.getMovie().getDuration());
            showtime.setStartTime(newStartTime);
            showtime.setEndTime(newEndTime);
            needsConflictCheck = true;
        }

        if (request.getLanguage() != null) {
            showtime.setLanguage(request.getLanguage());
        }

        if (needsConflictCheck) {
            checkTimeConflict(newRoomId, newStartTime, newEndTime, showtimeId);
        }

        Showtime updatedShowtime = showtimeRepo.save(showtime);
        return convertToDTO(updatedShowtime);
    }

    // moi them ne
    @Transactional
    public void deleteShowtime(String showtimeId) {
        Showtime showtime = showtimeRepo.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found with id: " + showtimeId));

        // Kiểm tra ghế đã booking chưa
        if (showtimeSeatRepo.existsByShowtime_ShowtimeIdAndBookingIsNotNull(showtimeId)) {
            throw new IllegalArgumentException("Cannot delete: showtime has booked seats.");
        }

        // Xóa các ghế liên quan trước
        showtimeSeatRepo.deleteByShowtime_ShowtimeId(showtimeId);

        // Rồi mới xóa suất chiếu
        showtimeRepo.delete(showtime);
    }

//    private void validateShowtimeRequest(ShowtimeRequestDTO request) {
//        if (request.getMovieId() == null || request.getMovieId().trim().isEmpty()) {
//            throw new IllegalArgumentException("Movie ID is required");
//        }
//        if (request.getRoomId() == null || request.getRoomId().trim().isEmpty()) {
//            throw new IllegalArgumentException("Room ID is required");
//        }
//        if (request.getStartTime() == null || request.getStartTime().trim().isEmpty()) {
//            throw new IllegalArgumentException("Start time is required");
//        }
//        if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
//            throw new IllegalArgumentException("Language is required");
//        }
//
//        // Validate start time format and future time
//        try {
//            LocalDateTime startTime = LocalDateTime.parse(request.getStartTime());
//            if (startTime.isBefore(LocalDateTime.now())) {
//                throw new IllegalArgumentException("Start time must be in the future");
//            }
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Invalid start time format. Use: yyyy-MM-ddTHH:mm:ss");
//        }
//    }
    
    private void validateShowtimeRequest(ShowtimeRequestDTO request) {
        if (request.getMovieId() == null || request.getMovieId().trim().isEmpty())
            throw new IllegalArgumentException("Movie ID is required");
        if (request.getRoomId() == null || request.getRoomId().trim().isEmpty())
            throw new IllegalArgumentException("Room ID is required");
        if (request.getStartTime() == null || request.getStartTime().trim().isEmpty())
            throw new IllegalArgumentException("Start time is required");
        if (request.getLanguage() == null || request.getLanguage().trim().isEmpty())
            throw new IllegalArgumentException("Language is required");

        String raw = request.getStartTime();
        System.out.println("🕵️‍♂️ DEBUG | raw startTime từ FE: [" + raw + "]");

        try {
            // thử parse chuẩn ISO
            LocalDateTime parsed = LocalDateTime.parse(raw);
            System.out.println("✅ Parse OK với LocalDateTime.parse(raw): " + parsed);
            if (parsed.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Start time must be in the future");
            }
        } catch (Exception e1) {
            System.out.println("⚠️ Parse lỗi chuẩn ISO, thử cách khác...");

            try {
                // thử với format có khoảng trắng
                LocalDateTime parsedAlt = LocalDateTime.parse(
                    raw.replace(" ", "T"),
                    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
                );
                System.out.println("✅ Parse OK với bản thay khoảng trắng: " + parsedAlt);
            } catch (Exception e2) {
                System.out.println("❌ Parse thất bại toàn bộ: " + e2.getMessage());
                throw new IllegalArgumentException(
                    "Invalid start time format. Use: yyyy-MM-ddTHH:mm:ss | raw=" + raw
                );
            }
        }
    }


    private void checkTimeConflict(String roomId, LocalDateTime startTime, LocalDateTime endTime, String excludeShowtimeId) {
        List<Showtime> existingShowtimes = showtimeRepo.findByRoom_RoomId(roomId);

        for (Showtime existing : existingShowtimes) {
            if (excludeShowtimeId != null && existing.getShowtimeId().equals(excludeShowtimeId)) {
                continue;
            }

            LocalDateTime existingStart = existing.getStartTime();
            LocalDateTime existingEnd = existing.getEndTime();

            boolean overlaps =
                    (startTime.isAfter(existingStart) && startTime.isBefore(existingEnd)) ||
                    (endTime.isAfter(existingStart) && endTime.isBefore(existingEnd)) ||
                    (startTime.isBefore(existingStart) && endTime.isAfter(existingEnd)) ||
                    (startTime.equals(existingStart) || endTime.equals(existingEnd));

            if (overlaps) {
                throw new IllegalArgumentException(
                        String.format("Time conflict! Room '%s' is already booked from %s to %s for movie '%s'",
                                existing.getRoom().getName(),
                                existingStart,
                                existingEnd,
                                existing.getMovie().getTitle()
                        )
                );
            }
        }
    }

    // ===========================
    //  SẮP LỊCH TỪ CUỐI NGÀY VỀ ĐẦU NGÀY (rating cao về tối)
    // ===========================
    @Transactional
    public List<ShowtimeResponseDTO> generateOptimalScheduleForRoom(
            String roomId,
            LocalDate date,
            Double occupancyRateOptional,      // giữ tham số để không phá API, nhưng KHÔNG dùng nữa
            List<String> preferredMovieIds
    ) {
        if (date == null) throw new IllegalArgumentException("Tham số 'date' là bắt buộc (ví dụ: 2025-10-14).");
        if (roomId == null || roomId.trim().isEmpty()) throw new IllegalArgumentException("Tham số 'roomId' không được để trống.");

        // Khung giờ mở/đóng
        boolean isWeekend = date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
        LocalTime open  = isWeekend ? WEEKEND_OPEN  : WEEKDAY_OPEN;
        LocalTime close = isWeekend ? WEEKEND_CLOSE : WEEKDAY_CLOSE;

        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng: " + roomId));

        // Lấy phim (ưu tiên nếu có), sắp theo rating giảm dần (null xem như 0)
        List<Movie> allMovies = (preferredMovieIds != null && !preferredMovieIds.isEmpty())
                ? movieRepo.findAllById(preferredMovieIds)
                : movieRepo.findAll();
        if (allMovies.isEmpty()) throw new IllegalArgumentException("Không có phim nào khả dụng để lên lịch.");

        List<Movie> movies = allMovies.stream()
                .sorted(Comparator.comparing((Movie m) -> m.getRating() == null ? 0.0 : m.getRating()).reversed())
                .collect(Collectors.toList());

        // Suất đã có trong ngày (để tránh chồng giờ)
        List<Showtime> existing = showtimeRepo.findByRoom_RoomId(roomId).stream()
                .filter(st -> st.getStartTime() != null && st.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());

        LocalDateTime dayStart = date.atTime(open);
        LocalDateTime dayEnd   = date.atTime(close);

        // Ước lượng số slot để tính quota/film
        double avgDur = movies.stream().mapToInt(Movie::getDuration).average().orElse(120);
        int possibleSlots = Math.max(1, calculateMaxPossibleSlots(dayStart, dayEnd, avgDur));

        // Nếu phim >= slot => mỗi phim tối đa 1 suất; ngược lại chia đều
        AtomicInteger maxPerMovie = new AtomicInteger(
                (movies.size() >= possibleSlots)
                        ? 1
                        : (int) Math.ceil((double) possibleSlots / Math.max(1, movies.size()))
        );

        Map<String, Integer> used = new HashMap<>();
        String lastMovieId = null;

        // Con trỏ xếp từ CUỐI ngày về: làm tròn xuống mốc 15'
        LocalDateTime cursorEnd = dayEnd
                .withMinute((dayEnd.getMinute() / 15) * 15)
                .withSecond(0).withNano(0);

        List<ShowtimeResponseDTO> created = new ArrayList<>();
        int roundRobinIndex = 0; // quay vòng theo thứ tự rating cao -> thấp

        while (!cursorEnd.isBefore(dayStart.plusMinutes(1))) {
            boolean placed = false;

            // thử tối đa N lần (số phim) để tìm 1 phim phù hợp cho slot hiện tại
            for (int tries = 0; tries < movies.size(); tries++) {
                Movie candidate = movies.get((roundRobinIndex + tries) % movies.size());

                // quota / cooldown back-to-back
                if (candidate.getMovieId().equals(lastMovieId)) continue;
                if (used.getOrDefault(candidate.getMovieId(), 0) >= maxPerMovie.get()) continue;

                int dur = Math.max(1, candidate.getDuration());
                LocalDateTime start = cursorEnd.minusMinutes(dur);
                if (start.isBefore(dayStart)) continue; // không đủ chỗ

                // tránh chồng với suất có sẵn (strict overlap)
                LocalDateTime s = start, e = cursorEnd;
                boolean conflict = existing.stream()
                        .anyMatch(ex -> s.isBefore(ex.getEndTime()) && e.isAfter(ex.getStartTime()));
                if (conflict) continue;

                // OK: tạo suất
                String lang = selectLanguageByTime(start);
                Showtime st = Showtime.builder()
                        .movie(candidate).room(room)
                        .startTime(start).endTime(cursorEnd)
                        .language(lang)
                        .build();

                Showtime saved = showtimeRepo.save(st);
                existing.add(saved);
                created.add(convertToDTO(saved));

                used.merge(candidate.getMovieId(), 1, Integer::sum);
                lastMovieId = candidate.getMovieId();
                roundRobinIndex = (movies.indexOf(candidate) + 1) % movies.size();

                // Lùi sang slot trước đó = start - CLEANING, rồi làm tròn xuống 15’
                LocalDateTime nextEnd = start.minusMinutes(CLEANING_TIME);
                cursorEnd = nextEnd
                        .withMinute((nextEnd.getMinute() / 15) * 15)
                        .withSecond(0).withNano(0);

                placed = true;
                break;
            }

            if (!placed) {
                // Không film nào phù hợp cho slot này -> lùi 15’ và thử lại (snap xuống 15’)
                LocalDateTime tmp = cursorEnd.minusMinutes(15);
                cursorEnd = tmp
                        .withMinute((tmp.getMinute() / 15) * 15)
                        .withSecond(0).withNano(0);
            }

            // Nếu tất cả phim đều chạm quota nhưng vẫn còn thời gian -> nới quota +1 (vòng 2)
            boolean allMaxed = movies.stream()
                    .allMatch(m -> used.getOrDefault(m.getMovieId(), 0) >= maxPerMovie.get());
            if (allMaxed && !cursorEnd.isBefore(dayStart.plusMinutes(1))) {
                maxPerMovie.incrementAndGet();
            }
        }

        // Trả DTO theo thứ tự thời gian tăng dần cho UI
        created.sort(Comparator.comparing(ShowtimeResponseDTO::getStartTime));
        return created;
    }



    /**
     * Tính điểm ưu tiên của phim dựa trên nhiều yếu tố
     */
    private double calculateMoviePriority(Movie m, double targetOcc) {
        double score = 0.0;

        // 1) Rating (0–10) – trọng số bạn có thể chỉnh
        double r = m.getRating() != null ? m.getRating() : 0.0;
        score += r * 3.0;

        // 2) Thời lượng “đẹp”
        int d = Math.max(1, m.getDuration());
        if (d >= 90 && d <= 120) score += 10.0;
        else if (d >= 80 && d <= 140) score += 5.0;

        // 3) Độ mới
        if (m.getReleaseDate() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(m.getReleaseDate(), java.time.LocalDate.now());
            if (days <= 30) score += 15.0;
            else if (days <= 60) score += 8.0;
        }

        // 4) (Optional) revenue cache
        Double cached = revenueCache.get(m.getMovieId());
        if (cached != null) score += cached / 1000.0;

        return score;
    }

    // ====== Chọn phim cho thuật toán xếp NGƯỢC (cuối -> đầu) ======
    private Movie selectOptimalMovieReverse(
            List<Movie> movies,
            LocalDateTime slotEnd,
            Map<String,Integer> usageCount,
            String lastMovieId,
            LocalDateTime dayStart
    ) {
        // chỉ lấy phim còn đủ thời gian để đặt start >= dayStart đến slotEnd
        List<Movie> candidates = movies.stream()
                .filter(m -> slotEnd.minusMinutes(Math.max(1, m.getDuration())).isAfter(dayStart.minusMinutes(1)))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) return null;

        return candidates.stream().max(Comparator.comparingDouble(m -> {
            // base: rating + một chút ưu tiên thời lượng đẹp
            double base = (m.getRating() == null ? 0.0 : m.getRating()) * 3.0;
            int d = Math.max(1, m.getDuration());
            if (d >= 90 && d <= 120) base += 2.0;

            // phạt nếu dùng nhiều lần để trải đều
            int used = usageCount.getOrDefault(m.getMovieId(), 0);
            base -= used * 0.8;

            // phạt nặng nếu trùng ngay phim vừa xếp sau đó (tránh back-to-back)
            if (m.getMovieId().equals(lastMovieId)) base -= 5.0;

            return base;
        })).orElse(candidates.get(0));
    }

    /**
     * Chọn ngôn ngữ phù hợp với khung giờ
     */
    private String selectLanguageByTime(LocalDateTime timeSlot) {
        LocalTime time = timeSlot.toLocalTime();

        // Buổi sáng sớm: Vietsub
        if (time.isBefore(LocalTime.of(12, 0))) {
            return "Vietsub";
        }
        // Chiều tối: Xen kẽ
        else if (time.isBefore(LocalTime.of(18, 0))) {
            return time.getMinute() % 2 == 0 ? "Vietsub" : "Lồng Tiếng";
        }
        // Tối: Ưu tiên lồng tiếng (gia đình)
        else {
            return "Lồng Tiếng";
        }
    }

    /**
     * Tính số slot tối đa có thể có trong ngày
     */
    private int calculateMaxPossibleSlots(LocalDateTime start, LocalDateTime end, double avgDuration) {
        long totalMinutes = java.time.temporal.ChronoUnit.MINUTES.between(start, end);
        return (int) (totalMinutes / (avgDuration + CLEANING_TIME));
    }

    private ShowtimeResponseDTO convertToDTO(Showtime showtime) {
        ShowtimeResponseDTO dto = new ShowtimeResponseDTO();
        dto.setShowtimeId(showtime.getShowtimeId());
        dto.setStartTime(showtime.getStartTime());
        dto.setEndTime(showtime.getEndTime());
        dto.setLanguage(showtime.getLanguage());

        if (showtime.getRoom() != null) {
            ShowtimeResponseDTO.RoomDTO roomDTO = new ShowtimeResponseDTO.RoomDTO();
            roomDTO.setRoomId(showtime.getRoom().getRoomId());
            roomDTO.setName(showtime.getRoom().getName());
            roomDTO.setCapacity(showtime.getRoom().getCapacity());
            dto.setRoom(roomDTO);
        }

        if (showtime.getMovie() != null) {
            ShowtimeResponseDTO.MovieDTO movieDTO = new ShowtimeResponseDTO.MovieDTO();
            movieDTO.setMovieId(showtime.getMovie().getMovieId());
            movieDTO.setTitle(showtime.getMovie().getTitle());
            movieDTO.setDuration(showtime.getMovie().getDuration());
            dto.setMovie(movieDTO);
        }
        return dto;
    }

    // Helpers: snap thời gian cho đẹp
    /** Làm tròn lên mốc blockMinute (ví dụ 15’) để giờ đẹp: 08:00, 10:15, 12:30… */
    private static LocalDateTime snapUpToBlock(LocalDateTime t, int blockMinute) {
        int m = t.getMinute();
        int mod = m % blockMinute;
        int add = (mod == 0) ? 0 : (blockMinute - mod);
        return t.plusMinutes(add).withSecond(0).withNano(0);
    }

    /** Làm tròn xuống mốc blockMinute (ví dụ 15’) để đi lùi: 22:58 -> 22:45 */
    private static LocalDateTime snapDownToBlock(LocalDateTime t, int blockMinute) {
        int m = t.getMinute();
        int mod = m % blockMinute;
        return t.minusMinutes(mod).withSecond(0).withNano(0);
    }
}
