package softtech.server.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import softtech.server.enums.CustomerType;
import softtech.server.models.Customer;

public interface CustomerRepo extends JpaRepository<Customer, String> {
    boolean existsByEmail(String email);
    Customer findByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<Customer> findByCustomerId(String id);
    Customer findByAccount_AccountId(String accountId);

    @Query("SELECT c FROM Customer c WHERE " +
            "(:fullName IS NULL OR LOWER(COALESCE(c.fullName, '')) LIKE LOWER(CONCAT('%', :fullName, '%'))) AND " +
            "(:email IS NULL OR LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:phoneNumber IS NULL OR COALESCE(c.phoneNumber, '') LIKE CONCAT('%', :phoneNumber, '%')) AND " +
            "(:type IS NULL OR c.type = :type)")
    Page<Customer> findCustomersWithFilters(
            @Param("fullName") String fullName,
            @Param("email") String email,
            @Param("phoneNumber") String phoneNumber,
            @Param("type") CustomerType type,
            Pageable pageable);

    Customer findByPhoneNumber(String phoneNumber);
}