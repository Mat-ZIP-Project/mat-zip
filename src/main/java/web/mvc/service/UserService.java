package web.mvc.service;

import web.mvc.domain.User;
import web.mvc.dto.LoginRequest;
import web.mvc.dto.TokenResponse;
import web.mvc.exception.BasicException;

public interface UserService {

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(String refreshToken);
}
