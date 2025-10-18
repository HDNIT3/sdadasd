package softtech.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softtech.server.dto.BookingDTO.BookingResponse;
import softtech.server.dto.BookingDTO.CreateBookingRequest;
import softtech.server.exceptions.ValidationException;
import softtech.server.models.Booking;
import softtech.server.models.Seat;
import softtech.server.repositories.ServiceOrderRepo;
import softtech.server.services.BookingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "https://zzzzz-production.up.railway.app")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody CreateBookingRequest request) {
        try {
            boolean isCounterBooking = request.getIsCounterBooking();
            if (!isCounterBooking) {
                if (request.getCustomerId() == null || request.getCustomerId().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Customer ID is required"));
                }
            }
            else {
                if (request.getCashierId() == null || request.getCashierId().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Cashier ID is required"));
                }
            }
            if (request.getShowtimeId() == null || request.getShowtimeId().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Showtime ID is required"));
            }
            if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "At least one seat must be selected"));
            }

            Booking booking = bookingService.createBooking(request);

            BookingResponse response = new BookingResponse();
            response.setBookingId(booking.getBookingId());
            response.setTotalAmount(booking.getTotalPrice());


            List<BookingResponse.SeatInfo> seatInfos = booking.getSeats().stream()
                    .map(showtimeSeat -> {
                        Seat seat = showtimeSeat.getSeat();
                        return new BookingResponse.SeatInfo(
                                seat.getSeatId(),
                                seat.getPosition(),
                                seat.getPrice(),
                                seat.getType().toString()
                        );
                    })
                    .toList();

            response.setSeats(seatInfos);

            return ResponseEntity.ok(response);

        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "details", e.getErrors()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to create booking",
                    "message", e.getMessage()
            ));
        }
    }
}