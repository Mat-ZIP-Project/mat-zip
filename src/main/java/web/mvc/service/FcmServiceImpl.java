package web.mvc.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.FcmToken;
import web.mvc.domain.User;
import web.mvc.repository.FcmTokenRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmServiceImpl implements FcmService {

    private final FcmTokenRepository fcmTokenRepository;

    /**
     *  사용자의 FCM 토큰을 등록하거나 업데이트한다.
     */

    @Override
    @Transactional
    public void registerFcmToken(User user, String deviceToken) {
        fcmTokenRepository.findByDeviceToken(deviceToken).ifPresentOrElse(
                existingToken -> {
                    if (!existingToken.getUser().getId().equals(user.getId())) {
                        existingToken.setUser(user);
                        fcmTokenRepository.save(existingToken);
                        log.info("FCM token registered successfully", deviceToken, user.getId());
                    } else {
                        log.warn("FCM token already registered", deviceToken, user.getId());
                    }
                },
                () -> {
                    FcmToken fcmToken = FcmToken.builder()
                            .user(user)
                            .deviceToken(deviceToken)
                            .build();
                    fcmTokenRepository.save(fcmToken);
                    log.info("FCM token registered successfully", deviceToken, user.getId());
                }
        );
    }

    /**
     *  FCM 토큰을 DB에서 삭제 한다.
     */
    @Override
    @Transactional
    public void unregisterFcmToken(String deviceToken) {
        fcmTokenRepository.deleteByDeviceToken(deviceToken);
    }

    /**
     *  특정 사용자에게 FCM 푸시 알림을 전송
     */
    @Override
    public void sendNotificationToUser(User user, String title, String body) {
        // 해당 사용자의 모든 FCM 토큰을 DB에서 조회
        List<FcmToken> fcmTokens = fcmTokenRepository.findByUser(user);

        if (fcmTokens.isEmpty()) {
            log.info("사용자 ID '{}'에게 보낼 FCM 토큰이 없습니다. 알림을 전송하지 않습니다.", user.getId());
            return;
        }

        // 조회된 FcmToken 객체에서 디바이스 토큰 문자열만 추출
        List<String> registrationTokens = fcmTokens.stream()
                .map(FcmToken::getDeviceToken)
                .collect(Collectors.toList());

        // FCM 메시지 객체 구성 (Notification Payload)
        // setNotification을 사용하여 알림 제목과 본문을 설정합니다.
        // putData를 통해 추가 데이터를 포함할 수 있으며, 이는 클라이언트 서비스 워커에서 처리할 수 있습니다.
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(com.google.firebase.messaging.Notification.builder() // Firebase Admin SDK의 Notification 클래스
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("title", title) // 클라이언트에서 알림 클릭 시 추가 정보로 활용될 수 있는 데이터
                .putData("body", body)
                .addAllTokens(registrationTokens) // 전송할 모든 디바이스 토큰 추가
                .build();

        try {
            // 여러 디바이스 토큰에 한 번의 요청으로 메시지 전송 (멀티캐스트)
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            log.info("사용자 ID '{}'에게 FCM 알림 전송 결과: 성공 {}개, 실패 {}개",
                    user.getId(), response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                // 알림 전송에 실패한 토큰들을 처리 (예: 유효하지 않은 토큰은 DB에서 삭제)
                for (int i = 0; i < response.getResponses().size(); i++) {
                    if (!response.getResponses().get(i).isSuccessful()) { // 전송 실패 응답인 경우
                        String failedToken = registrationTokens.get(i); // 실패한 토큰 문자열
                        FirebaseMessagingException fme = (FirebaseMessagingException) response.getResponses().get(i).getException();

                        // 토큰이 더 이상 유효하지 않거나, 등록되지 않았거나, 형식이 잘못된 경우 DB에서 삭제
                        if (fme.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED || // 토큰이 등록 해지됨
                                fme.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT // 토큰 형식이 잘못됨
                                ) { // 토큰을 찾을 수 없음 (유효하지 않을 가능성)

                            log.warn("유효하지 않은 FCM 토큰 감지: '{}'. DB에서 삭제합니다. 사유: {}", failedToken, fme.getMessage());
                            // 해당 토큰을 DB에서 삭제하는 로직
                            fcmTokenRepository.deleteByDeviceToken(failedToken);
                        } else {
                            log.error("FCM 알림 전송 실패 원인 (토큰: '{}'): {}", failedToken, fme.getMessage());
                        }
                    }
                }
            }
        } catch (FirebaseMessagingException e) {
            // FCM 서비스 자체에 문제가 발생하여 메시지를 보낼 수 없는 경우
            log.error("FCM 알림 전송 중 심각한 오류 발생: {}", e.getMessage(), e);
        }
    }
}
