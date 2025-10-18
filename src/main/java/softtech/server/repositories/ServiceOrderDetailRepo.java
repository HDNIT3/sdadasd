package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import softtech.server.models.ServiceOrderDetail;

@Repository
public interface ServiceOrderDetailRepo extends JpaRepository<ServiceOrderDetail, String> {
}