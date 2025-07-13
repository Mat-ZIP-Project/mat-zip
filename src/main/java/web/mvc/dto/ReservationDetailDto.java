package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetailDto {
    private Long reservationId;
    private Long restaurantId;
    private String restaurantName;
    private LocalDate date;
    private LocalTime time;
    private int numPeople;
    private String status; // 예약 상태
    private String ownerNotes; // 가게 사장 메모 (선택 사항)
    private LocalDateTime createdAt;
    private String paymentStatus; // 결제 상태 (엔티티에 직접 없는 파생 정보)

    /// 예약자 정보
    private String userId;
    private String userName;
    private boolean noShow; // 예약자 노쇼 이력
}
