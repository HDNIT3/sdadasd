package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softtech.server.models.Account;

@Repository
public interface AccountRepo extends JpaRepository<Account, String> {
    boolean existsByUsername(String username);
    Account findByUsername(String username);
}
