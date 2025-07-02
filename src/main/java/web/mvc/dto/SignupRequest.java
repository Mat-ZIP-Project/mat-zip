package web.mvc.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    private String userId;
    private String password;
    private String name;
    private String phone;
    private Boolean termsAgreed;
    private Boolean privacyAgreed;
    private String role = "ROLE_USER";
    private String preferenceCategory; //"한식,양식" 형태로 최대 2개까지 콤마 구분

}