package softtech.server.dto.BookingDTO;

import lombok.Data;

import java.util.List;

@Data
public class CreateBookingRequest {
    private String showtimeId;
    private String customerId;
    private String cashierId;
    private List<String> seatIds;
    private String serviceOrderId;
    private String customerPhone;
    private Boolean isCounterBooking = false;
}
