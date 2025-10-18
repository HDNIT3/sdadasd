package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softtech.server.models.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

}