package softtech.server.dto.ServiceDTO;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceDTO {
    private String serviceId;
    private String name;
    private double price;
    private String description;
}
