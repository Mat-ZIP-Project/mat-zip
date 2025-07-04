package web.mvc.dto;

import lombok.*;
import web.mvc.domain.WaitingQueue;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitingMyPageResponseDto {

    private Long waitingId;
    private String restaurantName;
    private int waitingNumber;
    private int numPeople;
    private LocalDateTime waitTime;

    public static WaitingMyPageResponseDto from(WaitingQueue w) {
        return WaitingMyPageResponseDto.builder()
                .waitingId(w.getWaitingId())
                .restaurantName(w.getRestaurant().getRestaurantName())
                .waitingNumber(w.getWaitingNumber())
                .numPeople(w.getNumPeople())
                .waitTime(w.getWaitTime())
                .build();
    }
}
