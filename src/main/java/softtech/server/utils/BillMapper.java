package softtech.server.utils;

import softtech.server.dto.BookingDTO.BillSearchResponseDTO;
import softtech.server.dto.BookingDTO.BookedSeatDTO;
import softtech.server.dto.BookingDTO.ServiceDetailDTO;
import softtech.server.models.Bill;
import java.util.stream.Collectors;
import java.util.Collections;

public class BillMapper {
    
    public static BillSearchResponseDTO toSearchResponseDTO(Bill bill) {
        BillSearchResponseDTO.BillSearchResponseDTOBuilder builder = BillSearchResponseDTO.builder()
                .billId(bill.getBillId())
                .paymentMethod(bill.getPaymentMethod())
                .totalAmount(bill.getTotalAmount());
        
        if (bill.getEventApplied() != null) {
            builder.eventId(bill.getEventApplied().getEventId())
                   .eventName(bill.getEventApplied().getName())
            	   .discountPercent(bill.getEventApplied().getDiscountPercent());
        }
        
        if (bill.getServiceOrder() != null) {
			builder.serviceOrderId(bill.getServiceOrder().getOrderId());
			
			// Map service details
			if (bill.getServiceOrder().getOrderDetails() != null) {
				builder.serviceDetails(
					bill.getServiceOrder().getOrderDetails().stream()
						.map(detail -> ServiceDetailDTO.builder()
							.serviceId(detail.getService().getServiceId())
							.serviceName(detail.getService().getName())
							.quantity(detail.getQuantity())
							.unitPrice(detail.getUnitPrice())
							.totalPrice(detail.getQuantity() * detail.getUnitPrice())
							.build())
						.collect(Collectors.toList())
				);
			} else {
				builder.serviceDetails(Collections.emptyList());
			}
		} else {
			builder.serviceDetails(Collections.emptyList());
		}
        if (bill.getCashier() != null) {
			builder.cashier(bill.getCashier());
		}
        else {
        	builder.cashier(null);
        }
        
        if (bill.getBooking() != null) {
            builder.bookingId(bill.getBooking().getBookingId())
                   .bookingTotalPrice(bill.getBooking().getTotalPrice());
            
			// Map booked seats
			if (bill.getBooking().getSeats() != null) {
				builder.bookedSeats(
					bill.getBooking().getSeats().stream()
						.map(showtimeSeat -> BookedSeatDTO.builder()
							.seatId(showtimeSeat.getSeat().getSeatId())
							.position(showtimeSeat.getSeat().getPosition())
							.price(showtimeSeat.getSeat().getPrice())
							.seatType(showtimeSeat.getSeat().getType().toString())
							.build())
						.collect(Collectors.toList())
				);
			} else {
				builder.bookedSeats(Collections.emptyList());
			}

            if (bill.getBooking().getCustomer() != null) {
                builder.customerId(bill.getBooking().getCustomer().getCustomerId())
                       .customerFullName(bill.getBooking().getCustomer().getFullName())
                       .customerEmail(bill.getBooking().getCustomer().getEmail())
                       .customerPhoneNumber(bill.getBooking().getCustomer().getPhoneNumber())
                       .customerDateOfBirth(bill.getBooking().getCustomer().getDateOfBirth());
            }
            
            if (bill.getBooking().getShowtime() != null) {
                builder.showtimeId(bill.getBooking().getShowtime().getShowtimeId());
                builder.startTime(bill.getBooking().getShowtime().getStartTime());
                builder.endTime(bill.getBooking().getShowtime().getEndTime());
                if (bill.getBooking().getShowtime().getMovie() != null) {
                    builder.movieTitle(bill.getBooking().getShowtime().getMovie().getTitle());
                }
                
                if (bill.getBooking().getShowtime().getRoom() != null) {
                    builder.theaterName(bill.getBooking().getShowtime().getRoom().getName());
                }
            }
        } else {
			builder.bookedSeats(Collections.emptyList());
		}
        
        return builder.build();
    }
}