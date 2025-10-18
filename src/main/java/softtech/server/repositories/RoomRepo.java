package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import softtech.server.models.Room;

public interface RoomRepo extends JpaRepository<Room, String> {
}