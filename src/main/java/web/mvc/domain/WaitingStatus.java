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
@Table(name = "waiting_status")
public class WaitingStatus {

    @Id
    @Column(name = "restaurant_id")
    private Long restaurantId; // @MapsId와 함께 Restaurant 엔티티를 FK + PK로 사용

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private Integer waitingCount; // 실시간 대기 인원 (앞에 몇 팀 대기 중)

    private LocalDateTime updatedAt = LocalDateTime.now();

    @Version
    private Long version;
}
