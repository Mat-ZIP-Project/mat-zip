package web.mvc.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 식당 주인 - 웨이팅 현황 확인용
 */
@Getter
@Builder
public class WaitingSummaryDTO {
    private Long restaurantId;
    private Integer totalWaitingCount;    // 현재 WAITING 상태 인원 수
    private LocalDateTime lastUpdatedAt;
}
