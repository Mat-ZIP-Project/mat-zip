package web.mvc.service;

import web.mvc.domain.User;
import web.mvc.dto.LoginRequest;
import web.mvc.dto.TokenResponse;
import web.mvc.exception.BasicException;

public interface UserService {

    /** 로그인 */
    TokenResponse login(LoginRequest request);

    /** refreshToken 유효성 체크 후 accessToken 재발급 */
    TokenResponse refresh(String refreshToken);

    /** 로그아웃시 RefreshToken 삭제 */
    void logout(User user);
}
