package softtech.server.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softtech.server.dto.BookingDTO.CreateBookingRequest;
import softtech.server.enums.CustomerType;
import softtech.server.models.*;
import softtech.server.repositories.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepo bookingRepo;

    private final CustomerRepo customerRepo;

    private final BookingServiceRepo bookingServiceRepo;

    private final ShowtimeRepo showtimeRepo;

    private final ShowtimeSeatRepo showtimeSeatRepo;

    private final ServiceOrderRepo serviceOrderRepo;

    @Transactional
    public Booking createBooking(CreateBookingRequest request) {

        List<ShowtimeSeat> seats = showtimeSeatRepo.findAllById(request.getSeatIds());
        if (seats.isEmpty()) {
            throw new RuntimeException("No seats found with provided IDs");
        }

        Showtime showtime = showtimeRepo.findByShowtimeId(request.getShowtimeId());
        if (showtime == null) {
            throw new RuntimeException("Showtime not found with ID: " + request.getShowtimeId());
        }

        Customer customer = null;
        boolean isCounterBooking = request.getIsCounterBooking();

        if (!isCounterBooking) {
            // Booking online, lấy khách theo accountId
            customer = customerRepo.findByAccount_AccountId(request.getCustomerId());
            if (customer == null) {
                throw new RuntimeException("Customer not found with ID: " + request.getCustomerId());
            }
        } else {
            // Booking tại quầy, kiểm tra số điện thoại trước
            customer = customerRepo.findByPhoneNumber(request.getCustomerPhone());
            if (customer == null) {
                // Tạo mới nếu chưa tồn tại
                customer = Customer.builder()
                        .phoneNumber(request.getCustomerPhone())
                        .type(CustomerType.GUEST)
                        .loyaltyPoints(10) // Khởi tạo 10 điểm cho lần đầu
                        .build();
                customerRepo.save(customer);
            } else {
                // Nếu đã tồn tại, cộng thêm 10 điểm
                int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
                customer.setLoyaltyPoints(currentPoints + 10);
                customerRepo.save(customer);
            }
        }

        List<ShowtimeSeat> showtimeSeats = new ArrayList<>();
        double totalAmount = 0.0;
        for (String seatId : request.getSeatIds()) {
            ShowtimeSeat showtimeSeat = showtimeSeatRepo.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Seat not found: " + seatId));

            if (!"AVAILABLE".equalsIgnoreCase(showtimeSeat.getStatus()) &&
                    !"LOCKED".equalsIgnoreCase(showtimeSeat.getStatus())) {
                throw new RuntimeException("Seat " + showtimeSeat.getSeat().getPosition() + " is not available");
            }

            showtimeSeats.add(showtimeSeat);
            totalAmount += showtimeSeat.getSeat().getPrice();
        }

        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setCustomer(customer);
        booking.setSeats(seats);
        booking.setTotalPrice(totalAmount);

        bookingRepo.save(booking);

        for (ShowtimeSeat showtimeSeat : showtimeSeats) {
            showtimeSeat.setStatus("OCCUPIED");
            showtimeSeat.setBooking(booking);
            showtimeSeatRepo.save(showtimeSeat);
        }

        if (request.getServiceOrderId() != null && !request.getServiceOrderId().isEmpty()) {
            addServiceToBooking(booking, request.getServiceOrderId());
        }

        return booking;
    }

    @Transactional
    public void addServiceToBooking(Booking booking, String serviceOrderId) {
        ServiceOrder order = serviceOrderRepo.findByOrderId(serviceOrderId);
        if (order == null) {
            throw new RuntimeException("Service order not found: " + serviceOrderId);
        }

        double additionalTotalPrice = order.getOrderDetails().stream()
                .mapToDouble(d -> d.getUnitPrice() * d.getQuantity())
                .sum();

        System.out.println("💰 Additional service price: $" + additionalTotalPrice);

        booking.setTotalPrice(booking.getTotalPrice() + additionalTotalPrice);

        softtech.server.models.BookingService bookingService = new softtech.server.models.BookingService();
        bookingService.setBooking(booking);
        bookingService.setServiceOrder(order);

        bookingRepo.save(booking);
        bookingServiceRepo.save(bookingService);

        System.out.println("✅ Updated booking total: $" + booking.getTotalPrice());
    }
    
    public Booking getBooking(String bookingId) {
		return bookingRepo.findById(bookingId).orElse(null);
	}
}