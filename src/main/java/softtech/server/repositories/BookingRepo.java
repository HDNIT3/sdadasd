package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import softtech.server.models.Booking;

public interface BookingRepo extends JpaRepository<Booking, String> {
}