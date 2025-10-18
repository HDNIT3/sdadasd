package softtech.server.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "shifts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String shiftId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private LocalDate workDate;
    @Column(nullable = false)
    private LocalTime startTime;
    @Column(nullable = false)
    private LocalTime endTime;
    private boolean isWeekend;
    private String shiftStatus;
    private String note;

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftRegistration> shiftRegistrations;
}

