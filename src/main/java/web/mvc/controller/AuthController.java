package web.mvc.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.*;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.SmsVerificationService;
import web.mvc.service.TokenService;
import web.mvc.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final ModelMapper modelMapper;
    private final UserService userService;
    private final TokenService tokenService;
    private final SmsVerificationService smsService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody TokenResponse body) {
        return ResponseEntity.ok(
                tokenService.refreshTokens(body.getRefreshToken())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails principal) {
        tokenService.invalidateToken(principal.getUser());
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
