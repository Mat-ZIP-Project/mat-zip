package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreparationReqDto {

    // 예약 Id
    private Long reservationId;
    // 예약금(결제 예정 금액)
    private Integer amount;
}
