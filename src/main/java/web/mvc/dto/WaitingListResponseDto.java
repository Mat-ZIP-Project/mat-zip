package web.mvc.dto;

import lombok.*;
import web.mvc.domain.WaitingQueue;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitingListResponseDto {

    private Long waitingId;
    private String userName;
    private String userPhone;
    private int numPeople;
    private int waitingNumber;
    private LocalDateTime waitTime;

    public static WaitingListResponseDto from(WaitingQueue w) {
        return WaitingListResponseDto.builder()
                .waitingId(w.getWaitingId())
                .userName(w.getUser().getName())
                .userPhone(w.getUser().getPhone())
                .numPeople(w.getNumPeople())
                .waitingNumber(w.getWaitingNumber())
                .waitTime(w.getWaitTime())
                .build();
    }
}
