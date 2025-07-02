package web.mvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.RefreshToken;
import web.mvc.domain.User;
import web.mvc.dto.*;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.RefreshTokenRepository;
import web.mvc.repository.SmsVerificationRepository;
import web.mvc.repository.UserRepository;
import web.mvc.security.CustomUserDetails;
import web.mvc.security.JwtTokenProvider;

import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SmsVerificationRepository smsVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final SignupService signupService;

    @Override
    public FindIdResponse findUserId(String phone) {
        // SMS 인증 완료 여부 확인
        smsVerificationRepository.findLatestVerifiedSms(phone, "ID_FIND")
                .orElseThrow(() -> new BasicException(ErrorCode.SMS_VERIFICATION_REQUIRED));

        // 휴대폰번호로 사용자 조회
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BasicException(ErrorCode.PHONE_NOT_FOUND));

        // 아이디 마스킹 처리
        String maskedUserId = maskUserId(user.getUserId());

        log.info("아이디 찾기 완료 - phone: {}, userId: {}", phone, user.getUserId());
        return new FindIdResponse(user.getUserId(), maskedUserId);
    }

    @Override
    public void validateUserForPasswordReset(FindPasswordRequest request) {
        // 아이디로 사용자 조회
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new BasicException(ErrorCode.USER_NOT_FOUND));

        // 휴대폰번호 매칭 확인
        if (!user.getPhone().equals(request.getPhone())) {
            throw new BasicException(ErrorCode.INVALID_REQUEST);
        }

        log.info("비밀번호 재설정 대상 확인 완료 - userId: {}, phone: {}",
                request.getUserId(), request.getPhone());
    }

    @Override
    @Transactional
    public void resetPassword(FindPasswordRequest request) {

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new BasicException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
        // SMS 인증 완료 여부 확인
        smsVerificationRepository.findLatestVerifiedSms(request.getPhone(), "PASSWORD_RESET")
                .orElseThrow(() -> new BasicException(ErrorCode.SMS_VERIFICATION_REQUIRED));

        // 사용자 조회 및 휴대폰번호 재확인
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new BasicException(ErrorCode.USER_NOT_FOUND));
        if (!user.getPhone().equals(request.getPhone())) {
            throw new BasicException(ErrorCode.INVALID_REQUEST);
        }

        // 새 비밀번호 유효성 검증
        signupService.validatePassword(request.getNewPassword());

        // 비밀번호 변경 (암호화)
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 모든 refresh 토큰 무효화
        tokenService.invalidateToken(user);

        log.info("비밀번호 재설정 완료 - userId: {}", request.getUserId());
    }

    @Override
    @Transactional
    public void withdrawUser(User user, WithdrawUserRequest request) {
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BasicException(ErrorCode.PASSWORD_MISMATCH);
        }
        user.setUserStatus("탈퇴");
        userRepository.save(user);

        tokenService.invalidateToken(user);

        log.info("회원탈퇴 완료 = {}", user.getUserId());
    }

    /** 아이디 마스킹 처리 (일부만 노출) */
    private String maskUserId(String userId) {
        if (userId.length() <= 3) {
            return userId.charAt(0) + "*".repeat(userId.length() - 1);
        }

        int visibleLength = Math.min(3, userId.length() / 2);
        String visible = userId.substring(0, visibleLength);
        String masked = "*".repeat(userId.length() - visibleLength);

        return visible + masked;
    }

}
