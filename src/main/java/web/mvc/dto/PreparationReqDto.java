package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreparationReqDto {

    //사용자 Id
    private Long Id;

    private Long restaurantId;
    private Long reservationId;

    private Integer numPeople;

    private String date;

    private String time;

    // 예약금(결제 예정 금액)
    private Integer amount;
}
