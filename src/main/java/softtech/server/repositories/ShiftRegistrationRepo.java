package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import softtech.server.models.Employee;
import softtech.server.models.Shift;
import softtech.server.models.ShiftRegistration;

import java.util.List;
import java.util.Optional;

public interface ShiftRegistrationRepo extends JpaRepository<ShiftRegistration, String> {
    long countByShift(Shift shift);
    Optional<ShiftRegistration> findByShiftAndEmployee(Shift shift, Employee employee);
    List<ShiftRegistration> findByShiftIn(List<Shift> shifts);
    @Query("""
        SELECT reg FROM ShiftRegistration reg
        JOIN FETCH reg.shift s
        WHERE reg.employee.employeeId = :employeeId
        ORDER BY s.workDate ASC, s.startTime ASC
    """)
    List<ShiftRegistration> findAllWithShiftByEmployeeId(@Param("employeeId") String employeeId);

    ShiftRegistration findByShift_ShiftId(String ShiftId);
}
