package softtech.server.dto.BookingDTO;

import lombok.*;
import softtech.server.enums.PaymentMethod;
import softtech.server.models.Employee;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillSearchResponseDTO {
    private String billId;
    private PaymentMethod paymentMethod;
    private double totalAmount;
    
    // Booking info
    private String bookingId;
    private double bookingTotalPrice;
    
    // Services info
    private String serviceOrderId;
    
    // Customer info
    private String customerId;
    private String customerFullName;
    private String customerEmail;
    private String customerPhoneNumber;
    private LocalDate customerDateOfBirth;
    
    // Showtime info (for context)
    private String showtimeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String movieTitle;
    private String theaterName;
    
    // Event info (if applicable)
    private String eventId;
    private String eventName;
    private double discountPercent;
    
    // Booked seats positions
    private List<BookedSeatDTO> bookedSeats;
    
    // Services with quantities
    private List<ServiceDetailDTO> serviceDetails;
    
    private Employee cashier;
}