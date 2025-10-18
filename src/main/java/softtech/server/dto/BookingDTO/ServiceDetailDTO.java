package softtech.server.dto.BookingDTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceDetailDTO {
    private String serviceId;
    private String serviceName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
}