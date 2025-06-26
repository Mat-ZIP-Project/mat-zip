package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "local_auths", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "region_name", "auth_date"})
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLocalAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authlogId;

    private String regionName;
    //@CreationTimestamp
    private LocalDate authDate; //년월일만

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private User user;
}
