package softtech.server.dto.BookingDTO;

import lombok.Data;
import softtech.server.models.Event;
import java.util.List;

@Data
public class CreateBillRequest {
    private String billId;
    private String bookingId;
    private List<String> seats; 
    private String paymentMethod; 
    private String serviceOrderId;

    private Double totalAmount;

  
    private String eventId;
    private Double discountPercent;
}