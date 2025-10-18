package softtech.server.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import softtech.server.dto.BookingDTO.BillSearchResponseDTO;
import softtech.server.models.Bill;
import softtech.server.repositories.BillRepo;
import softtech.server.utils.BillMapper;

@Service
public class TransactionService {
	@Autowired
	private BillRepo billRepo;
	

	public List<Bill> getAllBills() {
		return billRepo.findAll();
	}
	
	public Page<BillSearchResponseDTO> searchBillsByCustomerInfo(String email, String phoneNumber, Pageable pageable) {
		String searchEmail = (email != null && !email.trim().isEmpty()) ? email.trim() : null;
		String searchPhone = (phoneNumber != null && !phoneNumber.trim().isEmpty()) ? phoneNumber.trim() : null;
		Page<Bill> billPage = billRepo.findByCustomerEmailAndPhoneNumber(searchEmail, searchPhone, pageable);
		return billPage.map(BillMapper::toSearchResponseDTO);
	}
}