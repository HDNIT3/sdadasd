package softtech.server.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softtech.server.dto.BookingDTO.SeatSelectDTO;
import softtech.server.dto.BookingDTO.ShowtimeRequestDTO;
import softtech.server.dto.BookingDTO.ShowtimeResponseDTO;
import softtech.server.dto.ShowtimeDTO.ScheduleRequestDTO;
import softtech.server.models.Showtime;
import softtech.server.services.SeatService;
import softtech.server.services.ShowtimeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/showtime")
@CrossOrigin(origins = "https://zzzzz-production.up.railway.app")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;
    private final SeatService seatService;

    @GetMapping("/next14days")
    public ResponseEntity<?> getAllShowtimeForNext14Days() {
        try {
            List<ShowtimeResponseDTO> showtime = showtimeService.getAllShowtimesForNext14Days();

            if (showtime.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("There is no showtime in next 14 days");
            }

            return ResponseEntity.ok(showtime);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("error: " + e.getMessage());
        }
    }

    @GetMapping("/movie/{movieId}/next7days")
    public ResponseEntity<?> getShowtimeForNext7Days(@PathVariable String movieId) {
        try {
            List<ShowtimeResponseDTO> showtime = showtimeService.getShowtimesForNext7Days(movieId);

            if (showtime.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("There is no showtime in next 7 days for movie: " + movieId);
            }

            return ResponseEntity.ok(showtime);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("error: " + e.getMessage());
        }
    }

    @GetMapping("/movie/{movieId}/date")
    public ResponseEntity<?> getShowtimeByDate(
            @PathVariable String movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<ShowtimeResponseDTO> showtime = showtimeService.getShowtimesByDate(movieId, date);

            if (showtime.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("There is no showtime: " + date);
            }

            return ResponseEntity.ok(showtime);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("error: " + e.getMessage());
        }
    }

    @GetMapping("/{showtimeId}")
    public ResponseEntity<?> getShowtimeById(@PathVariable String showtimeId) {
        try {
            Showtime showtime = showtimeService.getShowtimeById(showtimeId);
            if (showtime == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Showtime not found: " + showtimeId);
            }
            return ResponseEntity.ok(showtime);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/movie/{movieId}/available-dates")
    public ResponseEntity<?> getAvailableDates(@PathVariable String movieId) {
        try {
            List<LocalDate> dates = showtimeService.getAvailableDatesForNext7Days(movieId);

            if (dates.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("There is no showtime available for today");
            }

            return ResponseEntity.ok(dates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("error: " + e.getMessage());
        }
    }

    @GetMapping("/movie/{movieId}/languages")
    public ResponseEntity<?> getAvailableLanguages(@PathVariable String movieId) {
        try {
            List<String> languages = showtimeService.getAvailableLanguages(movieId);
            if (languages.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("There is no information regarding subtitle");
            }

            return ResponseEntity.ok(languages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("error: " + e.getMessage());
        }
    }

    @GetMapping("/{showtimeId}/seats")
    public ResponseEntity<?> getSeatsByShowtime(@PathVariable String showtimeId) {
        List<SeatSelectDTO> seats = seatService.getSeatsByShowtime(showtimeId);
        if (seats.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "No seats found for showtime " + showtimeId));
        }
        return ResponseEntity.ok(seats);
    }

    @PostMapping
    public ResponseEntity<?> createShowtime(@RequestBody ShowtimeRequestDTO request) {
        try {
            ShowtimeResponseDTO created = showtimeService.createShowtime(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating showtime: " + e.getMessage());
        }
    }

    @PutMapping("/{showtimeId}")
    public ResponseEntity<?> updateShowtime(
            @PathVariable String showtimeId,
            @RequestBody ShowtimeRequestDTO request) {
        try {
            ShowtimeResponseDTO updated = showtimeService.updateShowtime(showtimeId, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating showtime: " + e.getMessage());
        }
    }

    @DeleteMapping("/{showtimeId}")
    public ResponseEntity<?> deleteShowtime(@PathVariable String showtimeId) {
        try {
            showtimeService.deleteShowtime(showtimeId);
            return ResponseEntity.ok("Showtime deleted successfully");
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting showtime: " + e.getMessage());
        }
    }

    @PostMapping("/generate-optimal-schedule")
    public ResponseEntity<?> generateOptimalSchedule(
            @RequestParam(required = false) String roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) List<String> movieIds,
            @RequestParam(required = false) Double occupancyRate
    ) {
        if (date == null) {
            return ResponseEntity.badRequest().body("⚠️ Tham số 'date' là bắt buộc (ví dụ: 2025-10-14).");
        }

        List<ShowtimeResponseDTO> schedule = showtimeService.generateOptimalScheduleForRoom(
                roomId,
                date,
                occupancyRate,
                movieIds
        );
        return ResponseEntity.ok(schedule);
    }
}
