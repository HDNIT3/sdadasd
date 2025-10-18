package softtech.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import softtech.server.dto.ServiceDTO.ServiceOrderDetailDTO;
import softtech.server.models.Customer;
import softtech.server.models.Employee;
import softtech.server.models.ServiceOrder;
import softtech.server.models.ServiceOrderDetail;
import softtech.server.repositories.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {
	private final ServiceOrderRepo serviceOrderRepo;
	private final ServiceOrderDetailRepo serviceOrderDetailRepo;
	private final CustomerRepo customerRepo;
	private final ServiceRepo serviceRepo;


	@Transactional
	public ServiceOrder createServiceOrder(String accountId, String phoneNumber) {

		Customer customer = customerRepo.findByAccount_AccountId(accountId);
		if (customer == null) {
			customer = customerRepo.findByPhoneNumber(phoneNumber);
		}

		ServiceOrder serviceOrder = ServiceOrder.builder()
				.customer(customer)
				.orderDetails(new ArrayList<>())
				.totalAmount(0.0)
				.build();

		serviceOrderRepo.save(serviceOrder);
		return serviceOrder;
	}

	@Transactional
	public void addServiceOrderDetails(String orderId, List<ServiceOrderDetailDTO> detailDTOs) {
		ServiceOrder serviceOrder = serviceOrderRepo.findByOrderId(orderId);
		if (serviceOrder == null) {
			throw new RuntimeException("Service Order not found");
		}

		List<ServiceOrderDetail> details = new ArrayList<>();
		double totalAmount = 0.0;

		for (ServiceOrderDetailDTO dto : detailDTOs) {
			softtech.server.models.Service service = serviceRepo.findByServiceId(dto.getServiceId())
					.orElseThrow(() -> new RuntimeException("Service not found with ID: " + dto.getServiceId()));

			ServiceOrderDetail detail = ServiceOrderDetail.builder()
					.serviceOrder(serviceOrder)
					.service(service)
					.quantity(dto.getQuantity())
					.unitPrice(service.getPrice())
					.build();

			details.add(detail);
			totalAmount += dto.getQuantity() * service.getPrice();
		}

		serviceOrderDetailRepo.saveAll(details);
		serviceOrder.setTotalAmount(totalAmount);
		serviceOrderRepo.save(serviceOrder);
	}

	public ServiceOrder getServiceOrderById(String orderId) {
		ServiceOrder serviceOrder = serviceOrderRepo.findByOrderId(orderId);
		if (serviceOrder == null) {
			throw new RuntimeException("Service Order not found");
		}
		return serviceOrder;
	}

	@Transactional
	public void deleteOrderDetailById(String id) {
		serviceOrderDetailRepo.deleteById(id);
	}

	@Transactional
	public void deleteServiceOrder(String orderId) {
		ServiceOrder order = serviceOrderRepo.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Service order not found"));

		serviceOrderDetailRepo.deleteAll(order.getOrderDetails());

		serviceOrderRepo.delete(order);
	}
}
