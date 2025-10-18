package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import softtech.server.enums.SeatStatus;
import softtech.server.models.Seat;

import java.util.List;

public interface SeatRepo extends JpaRepository<Seat, String> {
    List<Seat> findByRoom_RoomId(String roomId);
}