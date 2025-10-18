package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import softtech.server.models.ShowtimeSeat;

import java.util.List;

public interface ShowtimeSeatRepo extends JpaRepository<ShowtimeSeat, String> {

    List<ShowtimeSeat> getByShowtime_Room_RoomId(String showtimeRoomRoomId);
    ShowtimeSeat getBySeat_SeatId(String seatId);
    List<ShowtimeSeat> findByShowtime_ShowtimeId(String showtimeId);
    ShowtimeSeat findByShowtime_ShowtimeIdAndSeat_SeatId(String showtimeId, String seatId);

    // Thêm mới
    void deleteByShowtime_ShowtimeId(String showtimeId);
    boolean existsByShowtime_ShowtimeIdAndBookingIsNotNull(String showtimeId);
}
