package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCompleteResDto {
    private String merchantUid;
    private String impUid;
    private String status;
    private String message;
    private boolean success;
}
