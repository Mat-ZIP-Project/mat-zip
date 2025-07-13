package web.mvc.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자가 내 웨이팅 확인
 */
@Getter
@Builder
public class WaitingStatusResponseDTO {
    private String restaurantName;           // 식당 이름
    private Integer waitingNumber;           // 내 번호
    private Integer waitingOrder;            // 현재 순서
    private String status;                   // WAITING, ENTERED, NOSHOW
    private LocalDateTime expectedEntryTime; // 예상 입장 시간
    private Integer waitingCount;            // 현재 나보다 앞에 몇 팀
}
