package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.ReqCustomDTO;
import web.mvc.dto.ReqTempDTO;
import web.mvc.dto.ResTempDTO;
import web.mvc.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    /**
     * Temp 코스 검색
     */
    @GetMapping("/temp")
    public ResponseEntity<?> searchTempCourse(/*@AuthenticationPrincipal*/@RequestParam Long id ) {
        List<ResTempDTO> list = courseService.searchTempCourses(id);
        return ResponseEntity.ok().body(list);
    }

    /**
     * Temp 코스에 추가
     */
    @PostMapping("/temp")
    public ResponseEntity<?> insertTempCourse(/*@AuthenticationPrincipal*/ @RequestParam Long id, @RequestBody ReqTempDTO course) {
        course.setId(id);
        return ResponseEntity.ok().body(courseService.insertTempCourse(course));
    }

    /**
     * Custom 코스 저장
     */
    @PostMapping("/custom")
    public ResponseEntity<?> saveCustomCourse(@RequestParam Long id,@RequestBody List<ReqCustomDTO> list ) {
        list.forEach(dto->dto.setId(id));
        return ResponseEntity.ok(courseService.insertCustomCourse(list));
    }
    /**
     * custom 코스 상세보기
     */
    @GetMapping("/custom/details")
    public ResponseEntity<?> searchCustomCourseDetails(@RequestParam Long id,@RequestParam Long courseId) {
        return ResponseEntity.ok(courseService.searchCustomCourse(id,courseId));
    }
}
