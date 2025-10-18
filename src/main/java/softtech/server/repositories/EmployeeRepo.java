package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softtech.server.models.Employee;

import java.util.List;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, String> {

    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);

    // Tìm theo account_id (đang dùng)
    Employee findByAccount_AccountId(String accountId);

    // [THÊM] Fallback: tìm theo account.username (trường hợp JWT chứa username)
    Employee findByAccount_Username(String username);

    boolean existsByEmailAndEmployeeIdNot(String email, String employeeId);
    boolean existsByPhoneNumberAndEmployeeIdNot(String phoneNumber, String employeeId);

    List<Employee> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContaining(
            String fullName, String email, String phoneNumber
    );
}
