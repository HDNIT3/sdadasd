package softtech.server.dto.ShiftDTO;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AssignmentResult {
    private int totalShifts;
    private int assignedCount;
    private int totalApprovedCount;
    private Map<String, Double> employeeHoursWorked;
    private Map<String, String> unfulfilledShifts;
    private Map<String, Integer> employeeWorkload;
    private int totalEmployees;
    private Map<String, Integer> constraints;
}
