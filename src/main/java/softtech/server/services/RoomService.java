package softtech.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import softtech.server.models.Room;
import softtech.server.repositories.RoomRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepo roomRepo;

    public List<Room> getAllRooms() {
        return roomRepo.findAll();
    }

    public Room getRoomById(String roomId) {
        return roomRepo.findById(roomId).orElse(null);
    }
}
