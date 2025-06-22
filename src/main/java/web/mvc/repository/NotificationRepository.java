package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.Notification;
import web.mvc.domain.User;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    // 특정 사용자의 알림 조회
    List<Notification> findByUser(User user);

    // 특정 사용자의 활성화된 알림 조회 (FCM 토큰을 위함)
    Optional<Notification> findByUserAndIsActive(User user,boolean isActive);

    // FCM 토큰으로 알림 조회
    Optional<Notification> findByFcmToken(String fcmToken);
}
