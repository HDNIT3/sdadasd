package softtech.server.dto.ShiftDTO;

import lombok.Data;

@Data
public class AssignShiftRequest {
    private String employeeId;
    private String shiftId;
}
