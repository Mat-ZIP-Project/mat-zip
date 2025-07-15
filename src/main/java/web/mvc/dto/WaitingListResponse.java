package web.mvc.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitingListResponse {
    private Long waitingId;
    private String userId;
    private String userName;
    private String phone;
    private Integer numPeople;  // 대기 인원
    private LocalDateTime waitTime;
    private Integer waitingNumber;  // 대기번호
    private String status;  // "입장 대기", "호출", "입장 완료", "노쇼"
}
