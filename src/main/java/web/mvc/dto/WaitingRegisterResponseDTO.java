package web.mvc.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 등록 성공 후, 내 순번/예상 대기 시간 등 알려주는 DTO
 */

@Getter
@Builder
public class WaitingRegisterResponseDTO {
    private Integer waitingNumber;           // 내 대기 번호 (7번 등)
    private Integer waitingOrder;            // 현재 몇 번째 순서 (3번째 대기 등)
    private LocalDateTime expectedEntryTime; // 예상 입장 시각
    private String status;                   // 상태 - 입장대기, 입장완료, 노쇼
}
