package softtech.server.dto.ServiceDTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOrderDTO {
    private String orderId;
    private String customerId;
    private List<ServiceOrderDetailDTO> orderDetails;
    private double totalAmount;
}
