package softtech.server.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softtech.server.models.Service;

@Repository
public interface ServiceRepo extends JpaRepository<Service, String> {
	Optional<Service> findByServiceId(String serviceId);
}
