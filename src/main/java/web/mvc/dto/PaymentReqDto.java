package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentReqDto {
    private String impUid;
    private String merchantUid;

    private String date;
    private String time;
    private Integer numPeople;

}
