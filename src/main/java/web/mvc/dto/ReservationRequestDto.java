package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@Setter
public class ReservationRequestDto {

    //사용자 Id
    private Long Id;

    private Long restaurantId;

    private Integer numPeople;

    private String date;

    private String time;

    // 예약금
    private Integer amount;
}
