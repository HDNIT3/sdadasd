package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softtech.server.models.Event;

@Repository
public interface PromotionRepository extends JpaRepository<Event, String> {

}