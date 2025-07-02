package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCompleteReqDto {

    private String merchantUid;
    private String impUid;
    private String status;
    private String message;
    private boolean success;

}
