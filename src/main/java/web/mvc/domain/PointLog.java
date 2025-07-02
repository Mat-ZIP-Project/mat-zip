package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name = "point_logs")
public class PointLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_log_id")
    private Long pointLogId;

    // 적립(true) / 차감(false)
    @Column(name = "is_earned", nullable = false)
    private boolean isEarned;

    // 포인트 증감액 (+100, -500 등)
    @Column(nullable = false)
    private int amount;

    // 누적포인트(변동 후)
    @Column(name = "point_log", nullable = false)
    private int pointLog;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private User user;
}
