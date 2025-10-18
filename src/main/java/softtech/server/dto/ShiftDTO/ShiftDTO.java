package softtech.server.dto.ShiftDTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ShiftDTO {
    private String shiftId;
    private String name;
    private String startTime;
    private String endTime;
    private String note;
    private String shiftStatus;
    private boolean isWeekend;
    private List<RegistrationDTO> registrations;
}
