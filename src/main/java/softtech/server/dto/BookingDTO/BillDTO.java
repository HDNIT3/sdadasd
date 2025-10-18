package softtech.server.dto.BookingDTO;

import lombok.*;
import softtech.server.enums.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDTO {
    private String billId;
    private PaymentMethod paymentMethod;
    private double totalAmount;

    private String bookingId;
    private List<String> seats;
    private String movieTitle;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String customerName;

    private List<OrderDetailDTO> orderDetails;

    private String Event;
}