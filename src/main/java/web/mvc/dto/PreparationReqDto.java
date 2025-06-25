package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreparationReqDto {

    //사용자 Id
    private Long Id;
    // 식당 Id
    private Long restaurantId;
    // 예약 Id
    private Long reservationId;
    // 인원 수
    private Integer numPeople;
    // 예약 날짜
    private String date;
    // 예약 시간
    private String time;
    // 예약금(결제 예정 금액)
    private Integer amount;
}
