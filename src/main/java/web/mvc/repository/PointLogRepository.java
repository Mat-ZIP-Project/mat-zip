package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.PointLog;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {
}
