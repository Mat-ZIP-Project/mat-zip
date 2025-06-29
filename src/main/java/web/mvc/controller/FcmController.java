package web.mvc.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import web.mvc.domain.User;
import web.mvc.dto.FcmTokenReqDto;
import web.mvc.dto.NotificationResDto;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.UserRepository;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.FcmService;

@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
@Slf4j
public class FcmController {

    private final FcmService fcmService;
    private final UserRepository userRepository;

    /**
     * 클라이언트로부터 FCM 토큰을 받아 DB에 등록하는 메서드
     */
    @PostMapping("/registerToken")
    public ResponseEntity<?> registerToken(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody FcmTokenReqDto request) {
        try {
            fcmService.registerFcmToken(principal.getUser(), request.getDeviceToken());
            return ResponseEntity.ok().build();
        } catch (BasicException e) {
            if (e.getErrorCode() == ErrorCode.USER_NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     *  클라이언트의 FCM 토큰 해지 요청을 처리
     */
    @PostMapping("/unregisterToken")
    public ResponseEntity<?> unregisterToken(@RequestBody FcmTokenReqDto requestDto) {
        try {
            log.info("FCM 토큰 해지 요청 수신: 디바이스 토큰='{}'", requestDto.getDeviceToken());
            fcmService.unregisterFcmToken(requestDto.getDeviceToken());
            return ResponseEntity.ok().body("FCM 토큰이 성공적으로 해지되었습니다.");
        } catch (Exception e) {
            log.error("FCM 토큰 해지 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     *  특정 사용자에게 테스트 알림을 보내도록 FcmService를 호출
     */
    @PostMapping("/sendToNotification/{userId}")
    public ResponseEntity<?> sendTestNotification(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody NotificationResDto payload) {

        // 로그인된 사용자가 있는지 확인 (principal이 null이거나 인증되지 않은 경우)
        if (principal == null || principal.getUser() == null) {
            log.warn("인증되지 않은 사용자가 테스트 알림을 요청했습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인된 사용자만 테스트 알림을 보낼 수 있습니다.");
        }

        try {
            User targetUser = principal.getUser();

            log.info("테스트 알림 전송 요청: 사용자 ID='{}', 제목='{}', 내용='{}'", targetUser.getId(), payload.getTitle(), payload.getBody());
            fcmService.sendNotificationToUser(targetUser, payload.getTitle(), payload.getBody());
            return ResponseEntity.ok().body("테스트 알림 전송 요청이 처리되었습니다.");
        } catch (BasicException e) {
            log.error("테스트 알림 전송 실패: {}", e.getMessage());
            if (e.getErrorCode() == ErrorCode.USER_NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("알 수 없는 테스트 알림 전송 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}
