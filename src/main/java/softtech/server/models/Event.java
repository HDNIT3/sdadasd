package softtech.server.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "events")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String eventId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    private double discountPercent;

    @Column
    private LocalDate discountStartDate;

    @Column
    private LocalDate discountEndDate;

    @OneToMany(mappedBy = "eventApplied", cascade = CascadeType.ALL)
    private List<Bill> bills;
}
