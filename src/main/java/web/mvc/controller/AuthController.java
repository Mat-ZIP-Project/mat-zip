package web.mvc.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.*;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.SmsVerificationService;
import web.mvc.service.TokenService;
import web.mvc.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final ModelMapper modelMapper;
    private final UserService userService;
    private final TokenService tokenService;
    private final SmsVerificationService smsService;

    /** 토큰 갱신 (Refresh token 검증) */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 refreshToken 추출
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            throw new BasicException(ErrorCode.REFRESH_NOT_FOUND);
        }

        // 토큰 갱신
        TokenResponse tokenResponse = tokenService.refreshTokens(refreshToken);

        // 새로운 refreshToken을 쿠키에 설정
        Cookie newRefreshCookie = new Cookie("refreshToken", tokenResponse.getRefreshToken());
        newRefreshCookie.setHttpOnly(true);
        newRefreshCookie.setSecure(false); // 개발환경용
        newRefreshCookie.setMaxAge(14 * 24 * 60 * 60); // 14일
        newRefreshCookie.setPath("/");
        newRefreshCookie.setAttribute("SameSite", "Lax");
        response.addCookie(newRefreshCookie);

        // 응답에는 accessToken만 포함
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("accessToken", tokenResponse.getAccessToken());

        return ResponseEntity.ok(responseBody);
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails principal, HttpServletResponse response) {
        tokenService.invalidateToken(principal.getUser());

        // refreshToken 쿠키 삭제
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        response.addCookie(refreshCookie);

        log.info("로그아웃 완료 = {}", principal.getUser().getUserId());
        return ResponseEntity.ok().build();
    }

    /** 로그인된 사용자 정보 조회 */
    @GetMapping("/user-info")
    public ResponseEntity<UserDTO> getUser(@AuthenticationPrincipal CustomUserDetails principal) {
        //Modelmapper로 매핑
        UserDTO user = modelMapper.map(principal.getUser(), UserDTO.class);

        log.info("로그인 완료 = {}", principal.getUser().getUserId());
        return ResponseEntity.ok(user);
    }

    /** sms 인증 - 아이디 찾기용 */
    @PostMapping("/find-id/sms/send")
    public ResponseEntity<Void> sendFindIdSms(@RequestBody SendSmsRequest request) {
        smsService.sendVerificationCode(request.getPhone(), "ID_FIND");
        return ResponseEntity.ok().build();
    }

    /** 아이디 찾기 */
    @PostMapping("/find-id")
    public ResponseEntity<FindIdResponse> findUserId(@RequestBody FindIdRequest request) {
        FindIdResponse response = userService.findUserId(request.getPhone());
        return ResponseEntity.ok(response);
    }

    /** ID, 휴대폰 매칭 */
    @PostMapping("/find-password/validate")
    public ResponseEntity<Void> validateUserForPasswordReset(@RequestBody FindPasswordRequest request) {
        userService.validateUserForPasswordReset(request);
        return ResponseEntity.ok().build();
    }

    /** sms 인증 - 비밀번호 재설정용 */
    @PostMapping("/find-password/sms/send")
    public ResponseEntity<Void> sendPasswordResetSms(@RequestBody SendSmsRequest request) {
        smsService.sendVerificationCode(request.getPhone(), "PASSWORD_RESET");
        return ResponseEntity.ok().build();
    }

    /** 비밀번호 재설정 */
    @PostMapping("/find-password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody FindPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

}
