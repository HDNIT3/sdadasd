package softtech.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import softtech.server.dto.BookingDTO.BillSearchResponseDTO;
import softtech.server.models.Booking;
import softtech.server.services.BookingService;
import softtech.server.services.TransactionService;

@RestController
@RequestMapping("/api/Transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
	@Autowired
	private TransactionService transactionService;
	@Autowired
	public BookingService bookingService;
	
	@GetMapping("/bills/search")
	public ResponseEntity<Page<BillSearchResponseDTO>> searchBillsByCustomerInfo(
			@RequestParam(required = false) String email,
			@RequestParam(required = false) String phoneNumber,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
			Pageable pageable = PageRequest.of(page, size);
			Page<BillSearchResponseDTO> bills = transactionService.searchBillsByCustomerInfo(email, phoneNumber, pageable);
			return ResponseEntity.ok(bills);
	}
	
	
	
}