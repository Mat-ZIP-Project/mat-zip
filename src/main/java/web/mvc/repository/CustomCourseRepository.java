package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.CustomCourse;

public interface CustomCourseRepository extends JpaRepository<CustomCourse, Long> {
}
