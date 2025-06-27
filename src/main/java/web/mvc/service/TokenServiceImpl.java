package web.mvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        saveRefreshToken(user.getUserId(), refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /** 기존 토큰이 있으면 업데이트, 없으면 새로 생성 */
    @Transactional
    public void saveRefreshToken(String userId, String refreshToken) {
        // User ID로 영속성 컨텍스트에서 User 조회
        User managedUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BasicException(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.findByUser(managedUser)
                .ifPresentOrElse(
                        existing -> {
                            existing.setToken(refreshToken);
                            refreshTokenRepository.save(existing);
                        },
                        () -> {
                            RefreshToken newToken = RefreshToken.builder()  //엔티티에 insert
                                    .user(managedUser)
                                    .token(refreshToken)
                                    .build();
                            refreshTokenRepository.save(newToken);
                        }
                );
    }

    @Override
    public TokenResponse refreshTokens(String oldRefreshToken) {
        if (!jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new BasicException(ErrorCode.INVALID_TOKEN);
        }
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(oldRefreshToken)
                .orElseThrow(() -> new BasicException(ErrorCode.REFRESH_NOT_FOUND));

        User user = refreshTokenEntity.getUser();
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        refreshTokenEntity.setToken(newRefreshToken);
        refreshTokenRepository.save(refreshTokenEntity);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    @Transactional
    public void invalidateToken(User user) {
        refreshTokenRepository.findByUser(user)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        return jwtTokenProvider.validateToken(refreshToken) &&
                refreshTokenRepository.findByToken(refreshToken).isPresent();
    }
}