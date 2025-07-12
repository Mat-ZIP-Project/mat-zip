package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 사용자가 식당 웨이팅을 신청할 때 프론트에서 보내는 요청 형식
 */
@Getter
@Setter
public class WaitingRegisterRequestDTO {
    private Long restaurantId;   // 웨이팅할 식당 ID
    private Integer numPeople;   // 웨이팅 인원 수
}
