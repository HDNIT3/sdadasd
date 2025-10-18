package softtech.server.dto.BookingDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderDetailDTO {
    private String id;
    private String serviceName;
    private int quantity;
    private double price;
}