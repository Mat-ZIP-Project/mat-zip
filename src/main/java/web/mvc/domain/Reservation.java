package web.mvc.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    // 예약 인원 수
    private int numPeople;
    // 예약 날짜
    private String date;
    // 예약 시간
    private String time;

    // 예약 상태 ( 예약완료, 대기, 예약실패 )
    private String status;
    // 예약 신청 일
    private LocalDateTime createdAt;
    //예약 수정 일
    private LocalDateTime updatedAt;
    // 사장 예약 승인 일
    private LocalDateTime approvedAt;
    // 사장의 승인 설명 말
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
    }

    @PrePersist
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
