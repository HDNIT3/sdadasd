package softtech.server.dto.ServiceDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOrderDetailDTO {
    private String serviceId;
    private int quantity;
}