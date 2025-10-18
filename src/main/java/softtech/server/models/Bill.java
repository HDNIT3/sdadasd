package softtech.server.models;

import jakarta.persistence.*;
import lombok.*;
import softtech.server.enums.PaymentMethod;

@Entity
@Table(name = "bills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String billId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @OneToOne
    @JoinColumn(name = "bookingId")
    private Booking booking;

    @OneToOne
    @JoinColumn(name = "serviceOrderId")
    private ServiceOrder serviceOrder;

    @Column(nullable = false)
    private double totalAmount;

    @ManyToOne
    @JoinColumn(name = "eventId")
    private Event eventApplied;
    
    
    @ManyToOne
    @JoinColumn(name = "cashier", nullable = true)
    private Employee cashier;
}

