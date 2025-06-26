package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendSmsRequest {
    private String phone;
    private String purpose; // 'SIGNUP','PASSWORD_RESET','ID_FIND'
}
