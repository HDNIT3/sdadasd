package softtech.server.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import softtech.server.dto.BookingDTO.CreateBillRequest;
import softtech.server.dto.BookingDTO.BillDTO;
import softtech.server.models.Bill;
import softtech.server.services.BillService;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class BillController {
    @Autowired
    private BillService billService;

    @PostMapping
    public ResponseEntity<?> createBill(@RequestBody CreateBillRequest request) {
        try {
            BillDTO billDTO = billService.createBill(request);
            return ResponseEntity.ok(billDTO);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Failed to create bill", "error", e.getMessage()));
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<?> getBillsByAccount(@PathVariable String accountId) {
        try {
            List<BillDTO> bills = billService.getBillsByAccount(accountId);
            return ResponseEntity.ok(bills);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to fetch bills", "error", e.getMessage()));
        }
    }
    
    @GetMapping("/AddEmployee")
    public ResponseEntity<?> addEmployeeBill(@RequestParam String BillId,@RequestParam String EmID) {
		try {
			Bill bill = billService.getBillByIdAndAddEmployee(BillId, EmID);	
			
			return ResponseEntity.ok("message: Employee added to bill successfully");
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body(Map.of("message", "Failed to add employee to bill", "error", e.getMessage()));
		}
		
	}
}