package webprak.models;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "clients")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Client implements CommonEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String info;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;
}