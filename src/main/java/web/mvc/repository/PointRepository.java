package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.Point;

public interface PointRepository extends JpaRepository<Point, Long> {
}
