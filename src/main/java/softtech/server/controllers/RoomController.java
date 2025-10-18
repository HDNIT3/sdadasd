package softtech.server.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softtech.server.services.RoomService;

import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "https://zzzzz-production.up.railway.app")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<?> getAllRooms() {
        try {
            return ResponseEntity.ok(roomService.getAllRooms());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Failed to fetch rooms", "error", e.getMessage()));
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoomById(@PathVariable String roomId) {
        try {
            return ResponseEntity.ok(roomService.getRoomById(roomId));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Failed to fetch room", "error", e.getMessage()));
        }
    }
}
