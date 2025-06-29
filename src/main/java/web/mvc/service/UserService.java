package web.mvc.service;

import web.mvc.domain.User;
import web.mvc.dto.LoginRequest;
import web.mvc.dto.SignupRequest;
import web.mvc.dto.TokenResponse;
import web.mvc.exception.BasicException;

public interface UserService {

    /** 로그아웃시 RefreshToken 삭제 */
    void logout(User user);

    /** 회원가입 */
    void signUp(SignupRequest signupRequest);
}
