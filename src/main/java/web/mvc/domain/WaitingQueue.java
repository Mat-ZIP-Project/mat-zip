package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "waiting_queue",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"restaurant_id", "waiting_number"})
        }
)
public class WaitingQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long waitingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private Integer numPeople;

    private LocalDateTime waitTime = LocalDateTime.now(); // 웨이팅 등록 시각

    private LocalDateTime expectedEntryTime; // 예상 입장 시간 (optional)

    @Column(length = 20)
    private String status; // "입장 대기", "입장 완료", "노쇼"

    @Column(nullable = false)
    private Integer waitingNumber; // 본인 대기 번호 (1번, 2번, ...)

    private LocalDateTime calledAt;
}
