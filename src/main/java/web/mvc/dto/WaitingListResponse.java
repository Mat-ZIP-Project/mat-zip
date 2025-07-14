package web.mvc.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WaitingListResponse {
    private Long waitingId;
    private String userId;
    private String userName;
    private String phone;
    private Integer people;  // 대기 인원
    private LocalDateTime registeredAt;
    private Integer waitingNumber;  // 대기번호
    private String status;  // "입장 대기", "호출", "입장 완료", "노쇼"
}
