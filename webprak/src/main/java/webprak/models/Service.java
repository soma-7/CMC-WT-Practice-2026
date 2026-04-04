package webprak.models;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "services")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Service implements CommonEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "includes", columnDefinition = "jsonb")
    private String includes;

    @Column(name = "other", columnDefinition = "jsonb")
    private String other;
}