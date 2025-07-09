package web.mvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.RefreshToken;
import web.mvc.domain.User;
import web.mvc.dto.TokenResponse;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.RefreshTokenRepository;
import web.mvc.repository.UserRepository;
import web.mvc.security.JwtTokenProvider;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TokenResponse generateTokens(User user) {
        log.info("토큰 생성 시작: {}", user.getUserId());
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        saveRefreshToken(user.getUserId(), refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void saveRefreshToken(String userId, String refreshToken) {
        try {
            User user = userRepository.findActiveUserByUserId(userId)
                    .orElseThrow(() -> new BasicException(ErrorCode.USER_NOT_FOUND));

            RefreshToken existingToken = refreshTokenRepository.findByUser(user).orElse(null);

            if (existingToken != null) {
                // 기존 토큰 업데이트
                existingToken.setToken(refreshToken);
                refreshTokenRepository.save(existingToken);
            } else {
                // 새 토큰 생성
                RefreshToken newRefreshToken = RefreshToken.builder()
                        .user(user)
                        .token(refreshToken)
                        .build();
                refreshTokenRepository.save(newRefreshToken);
            }

            log.info("RefreshToken 저장 완료: userId={}", userId);

        } catch (Exception e) {
            log.error("RefreshToken 저장 실패: userId={}, error={}", userId, e.getMessage());
            throw new BasicException(ErrorCode.INVALID_TOKEN);
        }
    }

    @Override
    @Transactional
    public TokenResponse refreshTokens(String oldRefreshToken) {
        // 1. JWT 토큰 자체 유효성 검증
        if (!jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new BasicException(ErrorCode.INVALID_TOKEN);
        }

        // 2. DB 토큰 조회 및 만료시간 검증
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenAndNotExpired(oldRefreshToken, LocalDateTime.now())
                .orElseThrow(() -> new BasicException(ErrorCode.REFRESH_NOT_FOUND));

        User user = storedToken.getUser();

        // 3. 새로운 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        // 4. 토큰 로테이션 (기존 토큰 업데이트)
        storedToken.setToken(newRefreshToken);
        refreshTokenRepository.save(storedToken);
        log.info("토큰 갱신 완료: userId={}, 새로운만료시간={}", user.getUserId(), storedToken.getExpiresAt());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }


    @Override
    @Transactional
    public void invalidateToken(User user) {
        refreshTokenRepository.findByUser(user)
                .ifPresent(token -> {
                    refreshTokenRepository.delete(token);
                    log.info("토큰 무효화 완료: {}", user.getUserId());
                });

    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        return jwtTokenProvider.validateToken(refreshToken) &&
                refreshTokenRepository.findByToken(refreshToken).isPresent();
    }

}