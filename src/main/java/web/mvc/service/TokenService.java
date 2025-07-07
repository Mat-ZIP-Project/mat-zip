package web.mvc.service;

import web.mvc.domain.User;
import web.mvc.dto.TokenResponse;

public interface TokenService {
    /** Access Token과 Refresh Token을 생성 & Refresh Token을 DB에 저장 & 반환 */
    TokenResponse generateTokens(User user);

    /** Refresh Token을 검증하고 새로운 토큰들을 생성하여 갱신(토큰 로테이션) */
    TokenResponse refreshTokens(String refreshToken);

    /** RefreshToken DB 저장 */
    void saveRefreshToken(String userId, String refreshToken);

    /** 사용자의 Refresh Token을 DB에서 삭제 (로그아웃,비밀번호 변경,탈퇴) */
    void invalidateToken(User user);

    /** Refresh Token의 유효성 검증 - 만료시 false */
    boolean validateRefreshToken(String refreshToken);

}