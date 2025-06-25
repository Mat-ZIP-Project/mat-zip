package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.mvc.domain.FcmToken;
import web.mvc.domain.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    /**
     * 특정 사용자와 관련된 모든 FCM 토큰 목록을 조회
     */
    List<FcmToken> findByUser(User user);

    /**
     * 특정 디바이스 토큰으로 FCM 토큰 정보를 조회
     */
    Optional<FcmToken> findByDeviceToken(String deviceToken);

    /**
     * 특정 디바이스 토큰을 가진 FCM 토큰 정보를 삭제
     */
    void deleteByDeviceToken(String deviceToken);
}
