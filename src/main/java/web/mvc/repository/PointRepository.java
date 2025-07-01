package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import web.mvc.domain.Point;
import web.mvc.domain.User;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {

    List<Point> findByUserAndPointLog(User user, Point point);

    /**
     *  사용자의 모든 포인트 기록 중 가장 높은 pointLog 값을 조회
     */
    @Query("SELECT COALESCE(MAX(p.pointLog), 0) FROM Point p WHERE p.user = :user")
    int findMaxPointLogByUser(User user);
}
