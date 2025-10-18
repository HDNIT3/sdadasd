package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import softtech.server.models.BookingService;

public interface BookingServiceRepo extends JpaRepository<BookingService,String> {
}
