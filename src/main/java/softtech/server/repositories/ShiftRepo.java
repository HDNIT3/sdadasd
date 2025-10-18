package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import softtech.server.models.Shift;

import java.time.LocalDate;
import java.util.List;

public interface ShiftRepo extends JpaRepository<Shift, String> {
    boolean existsByWorkDateBetween(LocalDate startDate, LocalDate endDate);
    List<Shift> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);
    List<Shift> findByWorkDate(LocalDate workDate);
}
