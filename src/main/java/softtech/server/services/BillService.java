package softtech.server.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import softtech.server.dto.BookingDTO.BillDTO;
import softtech.server.dto.BookingDTO.CreateBillRequest;
import softtech.server.dto.BookingDTO.OrderDetailDTO;
import softtech.server.enums.PaymentMethod;
import softtech.server.enums.SeatStatus;
import softtech.server.models.*;
import softtech.server.repositories.*;

@Service
public class BillService {

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private BillRepo billRepo;

	@Autowired
	private ServiceOrderRepo serviceOrderRepo;
	
	@Autowired
	private ShowtimeSeatRepo showtimeseatRepo;
	
	@Autowired
	private EmployeeRepo employeeRepo;

    @Autowired
    private EventRepository eventRepository;

    @Transactional
    public Bill getBillByIdAndAddEmployee(String BillId, String AccId) {
        Bill a = billRepo.findById(BillId).orElse(null);
        Employee employee = employeeRepo.findByAccount_AccountId(AccId);
        if (a == null) {
            throw new IllegalArgumentException("Bill not found with ID: " + BillId);
        }
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found with Account ID: " + AccId);
        }
        a.setCashier(employee);
        return billRepo.save(a);
    }

    @Transactional
    public BillDTO createBill(CreateBillRequest request) {
        if (request.getBookingId() == null) {
            throw new IllegalArgumentException("Booking ID is required");
        }

        Booking booking = bookingRepo.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + request.getBookingId()));

        double baseAmount = booking.getTotalPrice();
        ServiceOrder serviceOrder = null;

        if (request.getServiceOrderId() != null && !request.getServiceOrderId().trim().isEmpty()) {
            serviceOrder = serviceOrderRepo.findByOrderId(request.getServiceOrderId());
            if (serviceOrder != null) {
                baseAmount += serviceOrder.getTotalAmount();
            } else {
                System.out.println("⚠️ Service order not found: " + request.getServiceOrderId());
            }
        }

        Event appliedEvent = null;
        double finalAmount = baseAmount;

        if (request.getEventId() != null && !request.getEventId().isBlank()) {
            appliedEvent = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new IllegalArgumentException("Event not found: " + request.getEventId()));

            if (isEventActive(appliedEvent, LocalDate.now())) {
                double percent = clampPercent(appliedEvent.getDiscountPercent());
                finalAmount = applyDiscount(baseAmount, percent);
            } else {
                System.out.println("⚠️ Event is not active today, ignore discount.");
            }
        } else if (request.getDiscountPercent() != null) {
            double percent = clampPercent(request.getDiscountPercent());
            finalAmount = applyDiscount(baseAmount, percent);
        } else if (request.getTotalAmount() != null) {
            // fallback: FE gửi sẵn tổng tiền
            finalAmount = request.getTotalAmount();
        }
		finalAmount = request.getTotalAmount();

        Bill bill = new Bill();
        bill.setBooking(booking);
        bill.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        bill.setTotalAmount(finalAmount);
        if (serviceOrder != null) {
            bill.setServiceOrder(serviceOrder);
        }
        if (appliedEvent != null) {
            bill.setEventApplied(appliedEvent);
        }

        Bill savedBill = billRepo.save(bill);

        updateSeatStatusToOccupied(booking);

        BillDTO dto = BillDTO.builder()
                .billId(savedBill.getBillId())
                .paymentMethod(savedBill.getPaymentMethod())
                .totalAmount(savedBill.getTotalAmount())
                .bookingId(booking.getBookingId())
                .customerName(booking.getCustomer() != null ? booking.getCustomer().getFullName() : null)
                .movieTitle(booking.getShowtime().getMovie().getTitle())
                .roomName(booking.getShowtime().getRoom().getName())
                .startTime(booking.getShowtime().getStartTime())
                .endTime(booking.getShowtime().getEndTime())
                .seats(booking.getSeats().stream().map(s -> s.getSeat().getPosition()).toList())
                .build();

        if (appliedEvent != null) {
            dto.setEvent(appliedEvent.getName());
        }

        if (savedBill.getServiceOrder() != null && savedBill.getServiceOrder().getOrderDetails() != null) {
            List<OrderDetailDTO> orderDetailDTOs = savedBill.getServiceOrder().getOrderDetails()
                    .stream()
                    .map(od -> new OrderDetailDTO(
                            od.getServiceOrderId(),
                            od.getService().getName(),
                            od.getQuantity(),
                            od.getService().getPrice() * od.getQuantity()))
                    .toList();
            dto.setOrderDetails(orderDetailDTOs);
        }

        return dto;
    }
    

    private boolean isEventActive(Event e, LocalDate today) {
        if (e.getDiscountStartDate() == null || e.getDiscountEndDate() == null) return false;
        return !today.isBefore(e.getDiscountStartDate()) && !today.isAfter(e.getDiscountEndDate());
    }

    private double clampPercent(double p) {
        return Math.min(Math.max(p, 0), 100);
    }

    private double applyDiscount(double base, double percent) {
        return Math.round(base * (1 - percent / 100.0) * 100.0) / 100.0;
    }

    private void updateSeatStatusToOccupied(Booking booking) {
        String bookingId = booking.getBookingId();
        System.out.println("🔄 Updating seat status for booking: " + bookingId);

        Booking freshBooking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        List<ShowtimeSeat> seats = freshBooking.getSeats();

        if (seats == null || seats.isEmpty()) {
            throw new RuntimeException("No seats found for booking " + bookingId);
        }

        for (ShowtimeSeat seat : seats) {
            ShowtimeSeat currentSeat = showtimeseatRepo.findById(seat.getShowtimeSeatId())
                    .orElseThrow(() -> new RuntimeException("Seat not found: " + seat.getShowtimeSeatId()));

            String oldStatus = currentSeat.getStatus();
            currentSeat.setStatus(SeatStatus.OCCUPIED.toString());
            showtimeseatRepo.save(currentSeat);

            System.out.println("✅ Seat " + currentSeat.getSeat().getPosition()
                    + " updated from " + oldStatus + " → " + SeatStatus.OCCUPIED);
        }

        System.out.println("✅ Successfully updated " + seats.size() + " seats to OCCUPIED status");
    }

    // ==================== GET BILLS ====================

    public List<BillDTO> getBillsByAccount(String accountId) {
        Customer customer = customerRepo.findByAccount_AccountId(accountId);
        if (customer == null) {
            return new ArrayList<>();
        }

        List<Bill> bills = billRepo.findByBooking_Customer_Account_AccountId(accountId);
        List<BillDTO> responses = new ArrayList<>();

        for (Bill b : bills) {
            BillDTO dto = new BillDTO();
            dto.setBillId(b.getBillId());
            dto.setPaymentMethod(b.getPaymentMethod());
            dto.setTotalAmount(b.getTotalAmount());

            if (b.getServiceOrder() != null && b.getServiceOrder().getOrderDetails() != null) {
                List<OrderDetailDTO> orderDetailDTOs = b.getServiceOrder().getOrderDetails()
                        .stream()
                        .map(od -> new OrderDetailDTO(
                                od.getServiceOrderId(),
                                od.getService().getName(),
                                od.getQuantity(),
                                od.getService().getPrice() * od.getQuantity()))
                        .toList();
                dto.setOrderDetails(orderDetailDTOs);
            }

            Booking booking = b.getBooking();
            dto.setBookingId(booking.getBookingId());
            dto.setMovieTitle(booking.getShowtime().getMovie().getTitle());
            dto.setStartTime(booking.getShowtime().getStartTime());
            dto.setEndTime(booking.getShowtime().getEndTime());
            dto.setSeats(
                    booking.getSeats()
                            .stream()
                            .map(showtimeSeat -> showtimeSeat.getSeat().getPosition())
                            .toList()
            );
            dto.setRoomName(booking.getShowtime().getRoom().getName());
            dto.setCustomerName(customer.getFullName());

            if (b.getEventApplied() != null) {
                dto.setEvent(b.getEventApplied().getName());
            }

            responses.add(dto);
        }

        return responses;
    }
}
