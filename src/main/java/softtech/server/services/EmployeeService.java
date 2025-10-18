package softtech.server.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import softtech.server.dto.StaffDTO.EmployeeDTO;
import softtech.server.enums.Role;
import softtech.server.models.Account;
import softtech.server.models.Employee;
import softtech.server.repositories.AccountRepo;
import softtech.server.repositories.EmployeeRepo;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private AccountRepo accountRepo;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public List<EmployeeDTO> search(String q) {
        List<Employee> list;
        if (q == null || q.isBlank()) {
            list = employeeRepo.findAll();
        } else {
            list = employeeRepo
                    .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContaining(q, q, q);
        }

        
        List<Employee> staffOnly = list.stream()
                .filter(e -> e.getAccount() != null && e.getAccount().getRole() == Role.STAFF)
                .collect(Collectors.toList());

        return staffOnly.stream().map(this::toDTO).collect(Collectors.toList());
    }


    public EmployeeDTO create(EmployeeDTO dto) {
        if (accountRepo.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (employeeRepo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (employeeRepo.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        Account acc = Account.builder()
                .username(dto.getUsername())
                .password(encoder.encode("User@123"))
                .role(Role.STAFF) 
                .build();
        Account savedAcc = accountRepo.save(acc);

        Employee emp = Employee.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .dateOfBirth(dto.getDateOfBirth())
                .account(savedAcc)
                .build();

        Employee saved = employeeRepo.save(emp);
        return toDTO(saved);
    }

    public EmployeeDTO update(String id, EmployeeDTO dto) {
        Employee emp = employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (employeeRepo.existsByEmailAndEmployeeIdNot(dto.getEmail(), id)) {
            throw new RuntimeException("Email already exists");
        }
        if (employeeRepo.existsByPhoneNumberAndEmployeeIdNot(dto.getPhoneNumber(), id)) {
            throw new RuntimeException("Phone number already exists");
        }

        emp.setFullName(dto.getFullName());
        emp.setEmail(dto.getEmail());
        emp.setPhoneNumber(dto.getPhoneNumber());
        emp.setDateOfBirth(dto.getDateOfBirth());

        Employee saved = employeeRepo.save(emp);
        return toDTO(saved);
    }

    private EmployeeDTO toDTO(Employee e) {
        return EmployeeDTO.builder()
                .employeeId(e.getEmployeeId())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .phoneNumber(e.getPhoneNumber())
                .dateOfBirth(e.getDateOfBirth())
                .position(null)
                .accountId(e.getAccount() != null ? e.getAccount().getAccountId() : null)
                .username(e.getAccount() != null ? e.getAccount().getUsername() : null)
                .role(e.getAccount() != null && e.getAccount().getRole() != null ? e.getAccount().getRole().name() : null)
                .build();
    }
}
