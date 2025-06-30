package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.CourseSpots;

public interface CourseSpotRepository extends JpaRepository<CourseSpots,Long> {
}
