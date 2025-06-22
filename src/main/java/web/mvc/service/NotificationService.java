package web.mvc.service;

import web.mvc.domain.User;

public interface NotificationService {
    // 사용자 FCM 토큰을 등록하거나 업데이트
    void registerOrUpdateFcmToken(User user, String fcmToken);
    // 사용자에게 알림 보냄
    void sendNotification(User user, String title, String body, String data);
}
