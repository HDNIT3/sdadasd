package softtech.server.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shift_registrations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employeeId", "shiftId"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ShiftRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String registrationId;

    @ManyToOne
    @JoinColumn(name = "employeeId", nullable = true)
    private Employee employee;
    @ManyToOne
    @JoinColumn(name = "shiftId", nullable = false)
    private Shift shift;

    @Column(nullable = false)
    private String note;
    private boolean isAutoAssigned = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}