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
public class Reservations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservation_id;

    // 예약 인원 수
    private int numPeople;

    // 예약 상태 ( 예약완료, 대기, 예약실패 )
    private String status;
    // 예약 신청 일
    private LocalDateTime createdAt;
    //예약 수정 일
    private LocalDateTime updatedAt;
    // 사장 예약 승인 일
    private LocalDateTime approvedAt;
}
