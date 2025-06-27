package web.mvc.service;

import org.springframework.http.ResponseEntity;
import web.mvc.dto.ReqCustomDTO;
import web.mvc.dto.ReqTempDTO;
import web.mvc.dto.ResCustomDTO;
import web.mvc.dto.ResTempDTO;

import java.util.List;

public interface CourseService {
    /**
     * Temp 코스 검색
     */
    List<ResTempDTO> searchTempCourses(Long id);
    /**
     * Temp 코스에 추가
     */
    String insertTempCourse(ReqTempDTO reqTempDTO);
    /**
     * Custom 코스 리스트 검색
     */
    List<ResCustomDTO> searchCustomCourseList(ReqTempDTO reqTempDTO);
    /**
     * Custom 코스 저장
     */
    String insertCustomCourse(List<ReqCustomDTO> list);
    /**
     * Custom 코스 검색
     */
    ResCustomDTO searchCustomCourse(Long id,Long courseId);
    /**
     * Custom 코스 수정하기
     */
    void updateCustomCourse(Long courseId, List<ReqCustomDTO> list);
    /**
     * Custom 코스 삭제하기
     */
    void deleteCustomCourse(Long courseId);
}
