package softtech.server.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import softtech.server.dto.BookingDTO.BillDTO;
import softtech.server.models.Account;
import softtech.server.models.Customer;
import softtech.server.repositories.CustomerRepo;
import softtech.server.services.BillService;
import softtech.server.services.CustomerService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
	private final CustomerRepo customerRepo;
	private final CustomerService customerService;
	private final BillService billService;

	@GetMapping
	public ResponseEntity<Page<Customer>> getAllCustomers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "customerId") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir, @RequestParam(required = false) String fullName,
			@RequestParam(required = false) String email, @RequestParam(required = false) String phoneNumber,
			@RequestParam(required = false) String type) {
		
		
		Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Customer> customers;

		if (type != null) {
			customers = customerService.searchCustomers(fullName, email, phoneNumber, type, pageable);
		} else {
			customers = customerService.getAllCustomers(pageable);
		}

		return ResponseEntity.ok(customers);
	}

	@PutMapping("/{customerId}")
	public ResponseEntity<Customer> updateCustomer(@PathVariable String customerId, @RequestBody Customer customer) {
		Optional<Customer> existing = customerRepo.findByCustomerId(customerId);
		if (existing.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		customer.setCustomerId(customerId);
		Customer updated = customerRepo.save(customer);
		return ResponseEntity.ok(updated);
	}

	@GetMapping("/transactions/{customerId}")
	public ResponseEntity<List<BillDTO>> getCustomerBills(@PathVariable String customerId,
			@RequestParam(required = false) String movieTitle) {
		
		Optional<Customer> customer = customerRepo.findByCustomerId(customerId);
		
		Optional<Account> account = customer.map(Customer::getAccount);

		List<BillDTO> bills = billService.getBillsByAccount(account.get().getAccountId());

		if (movieTitle != null && !movieTitle.isEmpty()) {
			bills = bills.stream().filter(b -> b.getMovieTitle().toLowerCase().contains(movieTitle.toLowerCase()))
					.toList();
		}

		return ResponseEntity.ok(bills);
	}
}