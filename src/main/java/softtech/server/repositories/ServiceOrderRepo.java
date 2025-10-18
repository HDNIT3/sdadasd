package softtech.server.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import softtech.server.models.ServiceOrder;


@Repository
public interface ServiceOrderRepo extends JpaRepository<ServiceOrder, String> {
	ServiceOrder findByOrderId(String orderId);
}
