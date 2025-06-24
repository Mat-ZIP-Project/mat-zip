package web.mvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.RefreshToken;
import web.mvc.domain.User;
import web.mvc.dto.LoginRequest;
import web.mvc.dto.TokenResponse;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.RefreshTokenRepository;
import web.mvc.repository.UserRepository;
import web.mvc.security.CustomUserDetails;
import web.mvc.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserId(), request.getPassword())
        );

        // principal에서 User 엔티티 직접 꺼내기
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User user = cud.getUser();

        String accessToken  = jwtTokenProvider.createAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        // 기존 토큰이 있으면 업데이트, 없으면 새로 생성
        refreshTokenRepository.findByUser(user)
            .ifPresentOrElse(
                    existing -> {
                        existing.setToken(refreshToken);  //update
                        refreshTokenRepository.save(existing);
                    },
                    () -> {
                        refreshTokenRepository.save( //엔티티에 insert
                                RefreshToken.builder()
                                        .user(user)
                                        .token(refreshToken)
                                        .build()
                        );
                    }
            );

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TokenResponse refresh(String oldRefresh) {
        if (!jwtTokenProvider.validateToken(oldRefresh)) {
            throw new BasicException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken entity = refreshTokenRepository.findByToken(oldRefresh)
                .orElseThrow(() -> new BasicException(ErrorCode.REFRESH_NOT_FOUND));

        String userId = jwtTokenProvider.getUserId(oldRefresh);
        String role   = userRepository.findById(entity.getUser().getId())
                .orElseThrow().getRole();

        String newAccess = jwtTokenProvider.createAccessToken(userId, role);

        return TokenResponse.builder()
                .accessToken(newAccess)
                .refreshToken(oldRefresh)
                .build();
    }
}
