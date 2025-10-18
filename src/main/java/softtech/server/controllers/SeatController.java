package softtech.server.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softtech.server.dto.BookingDTO.SeatSelectDTO;
import softtech.server.models.ShowtimeSeat;
import softtech.server.services.SeatService;
import softtech.server.utils.JwtUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "https://zzzzz-production.up.railway.app")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;
    private final JwtUtil jwtUtil;

    @GetMapping("/showtime/{showtimeId}")
    public ResponseEntity<List<SeatSelectDTO>> getSeatsByShowtime(@PathVariable String showtimeId) {
        try {
            List<SeatSelectDTO> seats = seatService.getSeatsByShowtime(showtimeId);
            return ResponseEntity.ok(seats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/lock")
    public ResponseEntity<?> lockSeats(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, List<String>> request) {

        try {
            String token = authHeader.replace("Bearer ", "");
            String accountId = jwtUtil.extractUserId(token);
            List<String> seatIds = request.get("seatIds");
            List<ShowtimeSeat> locked = seatService.lockSeats(seatIds, accountId);
            return ResponseEntity.ok(Map.of("success", true, "seats", locked));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/release")
    public ResponseEntity<?> releaseSeats(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, List<String>> request) {

        try {
            String token = authHeader.replace("Bearer ", "");
            String accountId = jwtUtil.extractUserId(token);
            List<String> seatIds = request.get("seatIds");
            seatService.releaseSeats(seatIds, accountId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Seats released successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/check-availability")
    public ResponseEntity<?> checkAvailability(@RequestBody Map<String, List<String>> request) {
        try {
            List<String> seatIds = request.get("seatIds");
            boolean available = seatService.areSeatsAvailable(seatIds);
            return ResponseEntity.ok(Map.of("available", available));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("available", false, "message", e.getMessage()));
        }
    }
}