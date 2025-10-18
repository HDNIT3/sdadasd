package softtech.server.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String bookingServiceId;

    @ManyToOne
    @JoinColumn(name = "bookingId")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "orderId")
    private ServiceOrder serviceOrder;
}
