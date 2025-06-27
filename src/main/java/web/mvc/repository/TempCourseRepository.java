package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.TempCourseItem;

public interface TempCourseRepository extends JpaRepository<TempCourseItem,Long> {
}
