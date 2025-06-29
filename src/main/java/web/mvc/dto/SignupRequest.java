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

    // 사업주 회원가입시에만 사용
    private String businessNumber;
}
