package softtech.server.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "service_order_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String serviceOrderId;

    @ManyToOne
    @JoinColumn(name = "orderId", nullable = false)
    @JsonBackReference
    private ServiceOrder serviceOrder;

    @ManyToOne
    @JoinColumn(name = "serviceId", nullable = false)
    private Service service;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double unitPrice;
}

