package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.mvc.domain.Notification;
import web.mvc.domain.User;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {

    /**
     *  특정 사용자를 위한 모든 알림을 생성일시 기준 내림차순으로 조회
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    /**
     *  사용자의 알림 내역 조회
     */
    List<Notification> findByUserId(Long id);

    /**
     *  사용자의 읽지 않은 알림 개수를 조회
     */
    List<Notification> findByUserIdAndIsRead(Long id, Boolean isRead);

    /**
     *  읽지 않은 알림 개수
     */
    int countByUserIdAndIsRead(Long id, Boolean isRead);
}
