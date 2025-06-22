package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortOneWebhookDto {

    // 아임포트 결제 고유 번호
    private String impUid;
    // 가맹점 주문 번호
    private String merchantUid;
    // 결제 상태
    private String status;
    // 실제 결제된 금액
    private Integer amount;
}
