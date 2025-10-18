package softtech.server.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import softtech.server.dto.BookingDTO.SeatSelectDTO;
import softtech.server.models.Seat;
import softtech.server.models.ShowtimeSeat;
import softtech.server.repositories.SeatRepo;
import softtech.server.repositories.ShowtimeSeatRepo;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final ShowtimeSeatRepo showtimeSeatRepo;
    private final SeatRepo seatRepo;

    public List<SeatSelectDTO> getSeatsByShowtime(String showtimeId) {
        List<SeatSelectDTO> responses = new ArrayList<>();
        List<ShowtimeSeat> showtimeSeats = showtimeSeatRepo.findByShowtime_ShowtimeId(showtimeId);

        for (ShowtimeSeat s : showtimeSeats) {
            SeatSelectDTO dto = new SeatSelectDTO();
            dto.setSeatId(s.getShowtimeSeatId());
            dto.setStatus(s.getStatus());
            dto.setLastUpdate(s.getLastUpdate());
            dto.setLockedBy(s.getLockedBy()); // thêm thông tin chính chủ

            Seat seat = seatRepo.findById(s.getSeat().getSeatId()).orElse(null);
            if (seat != null) {
                dto.setType(String.valueOf(seat.getType()));
                dto.setPrice(seat.getPrice());
                dto.setPosition(seat.getPosition());
            }

            responses.add(dto);
        }
        return responses;
    }

    @Transactional
    public List<ShowtimeSeat> lockSeats(List<String> seatIds, String accountId) {
        List<ShowtimeSeat> lockedSeats = new ArrayList<>();

        for (String seatId : seatIds) {
            ShowtimeSeat seat = showtimeSeatRepo.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Seat not found: " + seatId));

            if ("AVAILABLE".equalsIgnoreCase(seat.getStatus())) {
                seat.setStatus("LOCKED");
                seat.setLockedBy(accountId); // gắn accountId chính chủ
                lockedSeats.add(showtimeSeatRepo.save(seat));
            } else {
                throw new RuntimeException("Seat " + seat.getSeat().getPosition() + " is not available");
            }
        }

        return lockedSeats;
    }

    @Transactional
    public void releaseSeats(List<String> seatIds, String accountId) {
        for (String seatId : seatIds) {
            ShowtimeSeat seat = showtimeSeatRepo.findById(seatId).orElse(null);
            if (seat != null) {
                if ("LOCKED".equalsIgnoreCase(seat.getStatus())) {
                    // chỉ cho phép chính chủ release
                    if (accountId.equals(seat.getLockedBy())) {
                        seat.setStatus("AVAILABLE");
                        seat.setLockedBy(null);
                        showtimeSeatRepo.save(seat);
                    } else {
                        throw new RuntimeException("You cannot release a seat locked by another user: " + seat.getSeat().getPosition());
                    }
                }
            }
        }
    }

    public boolean areSeatsAvailable(List<String> seatIds) {
        for (String seatId : seatIds) {
            ShowtimeSeat seat = showtimeSeatRepo.findById(seatId).orElse(null);
            if (seat == null ||
                    (!"AVAILABLE".equalsIgnoreCase(seat.getStatus()) &&
                            !"LOCKED".equalsIgnoreCase(seat.getStatus()))) {
                return false;
            }
        }
        return true;
    }
}
