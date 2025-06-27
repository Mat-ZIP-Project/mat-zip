package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.mvc.domain.CustomCourse;
import web.mvc.dto.ResCustomDTO;

public interface CustomCourseRepository extends JpaRepository<CustomCourse, Long> {
    @Query("select cc " +
            "from CustomCourse cc " +
            "join fetch cc.courseSpotsList cs " +
            "join fetch cs.restaurant r " +
            "where cc.courseId=:courseId and cc.user.id=:id")
    CustomCourse searchCustomCourse(@Param("id") Long id,@Param("courseId") Long courseId);
}
