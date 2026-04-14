package webprak.models;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "operations")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Operation implements CommonEntity<Long> {

    public enum OperationType {
        deposit,
        withdrawal,
        service_payment,
        service_purchase,
        service_cancellation
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false)
    private OperationType type;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @Column(name = "balance_change", nullable = false)
    private Double balanceChange;

    @Column(name = "description")
    private String description;
}