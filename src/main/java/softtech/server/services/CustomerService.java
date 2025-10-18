package softtech.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import softtech.server.enums.CustomerType;
import softtech.server.models.Customer;
import softtech.server.repositories.CustomerRepo;

@Service
public class CustomerService {
    @Autowired
    CustomerRepo customerRepo;

    public Customer getCustomerByAccountId(String accountID) {
           return customerRepo.findByAccount_AccountId(accountID);
    }
    
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepo.findAll(pageable);
    }
    
    public Page<Customer> searchCustomers(String fullName, String email, String phoneNumber, String type, Pageable pageable) {
    	CustomerType customerType = null;
	    if (type != null && !type.trim().isEmpty()) {
	        try {
	            customerType = CustomerType.valueOf(type.toUpperCase());
	        } catch (IllegalArgumentException e) {
	            customerType = null;
	        }
	    }
	    
    	return customerRepo.findCustomersWithFilters(fullName, email, phoneNumber, customerType, pageable);
    }
}