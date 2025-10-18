package softtech.server.dto.StaffDTO;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private String employeeId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String position;

    private String accountId;
    private String username;
    private String role; // STAFF, MANAGER, ...
}