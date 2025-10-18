package softtech.server.dto.BookingDTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class BookingResponse {
    private String bookingId;
    private double totalAmount;
    private List<SeatInfo> seats;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SeatInfo {
        private String seatId;
        private String position;
        private double price;
        private String type;
    }
}