package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupDuplicateRequest {
    private String userId;
    private String phone;
    private String businessNumber;
}
