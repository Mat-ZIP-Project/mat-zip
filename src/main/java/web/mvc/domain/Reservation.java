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
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    // 예약 인원 수
    @Column(name = "num_people", nullable = false)
    private int numPeople;
    // 예약 날짜
    @Column(name = "reservation_date", nullable = false, length = 20)
    private String date;
    // 예약 시간
    @Column(name = "reservation_time", nullable = false, length = 20)
    private String time;

    // 예약 상태 ( 예약완료, 대기, 예약실패 )
    private String status;
    // 예약 신청 일
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    //예약 수정 일
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    // 사장 예약 승인 일
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    // 사장의 승인 설명 말
    @Column(name = "owner_notes")
    private String ownerNotes;

    // users 테이블의 id를 참조
    @ManyToOne
    @JoinColumn(name="id", nullable = false)
    private User user;
    // restaurants 테이블의 id 참조
    @ManyToOne
    @JoinColumn(name="restaurantId", nullable = false)
    private Restaurant restaurant;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null || this.status.isEmpty()) {
            this.status = "pending";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
