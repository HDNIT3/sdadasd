package softtech.server.dto.ShiftDTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RegistrationDTO {
    private String registrationId;
    private String shiftId;
    private String employeeId;
    private String note;
    private boolean isAutoAssign;
    private LocalDateTime updated_at;
    private LocalDateTime created_at;
}
