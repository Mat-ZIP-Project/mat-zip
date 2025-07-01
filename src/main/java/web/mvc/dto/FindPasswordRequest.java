package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindPasswordRequest {
    private String userId;
    private String phone;
    private String newPassword; // 재설정 시에만 사용 (비밀번호 찾기 검증 단계에서는 null)
}
