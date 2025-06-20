package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "notifications")
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notification_id;

    private boolean is_active;

    private String fcm_token;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservations reservations;

    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private User user;

}
