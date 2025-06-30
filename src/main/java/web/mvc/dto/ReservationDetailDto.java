package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetailDto {
    private Long reservationId;
    private String restaurantName;
    private LocalDateTime date;
    private LocalDateTime time;
    private int numPeople;
    private String status; // 예약 상태
    private String ownerNotes; // 가게 사장 메모 (선택 사항)
    private LocalDateTime createdAt;
    private String paymentStatus; // 결제 상태 (엔티티에 직접 없는 파생 정보)
}
