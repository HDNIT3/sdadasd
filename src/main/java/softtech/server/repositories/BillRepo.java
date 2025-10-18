package softtech.server.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import softtech.server.models.Bill;

public interface BillRepo extends JpaRepository<Bill, String> {
	List<Bill> findByBooking_Customer_Account_AccountId(String accountId);
	@Query("""
			SELECT b FROM Bill b
			WHERE
			    (:email IS NULL OR :email = '' OR LOWER(b.booking.customer.email) LIKE LOWER(CONCAT('%', :email, '%')))
			AND
			    (:phoneNumber IS NULL OR :phoneNumber = '' OR b.booking.customer.phoneNumber LIKE CONCAT('%', :phoneNumber, '%'))
			""")
	Page<Bill> findByCustomerEmailAndPhoneNumber(@Param("email") String email, @Param("phoneNumber") String phoneNumber,
			Pageable pageable);
}