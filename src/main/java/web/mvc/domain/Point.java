package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "point_logs")
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_log_id")
    private Long point_id;

    // 적립 = 1, 사용 = 0으로
    @Column(name = "is_earned", nullable = false)
    private boolean isEarned;

    // 적립 / 사용 한 금액량
    @Column(name = "amount")
    private int pointAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "point_log")
    private int pointLog;

    // users 테이블의 id를 참조
    @ManyToOne
    @JoinColumn(name="id", nullable = false)
    private User user;
}
