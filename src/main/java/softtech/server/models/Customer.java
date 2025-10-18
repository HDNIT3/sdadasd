package softtech.server.models;

import jakarta.persistence.*;
import lombok.*;
import softtech.server.enums.CustomerType;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String customerId;

    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private CustomerType type;

    @OneToOne
    @JoinColumn(name = "accountId")
    private Account account;

    private Integer loyaltyPoints;
}
