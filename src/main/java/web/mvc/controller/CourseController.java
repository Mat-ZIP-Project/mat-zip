package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
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
    @PostMapping("temp")
    public ResponseEntity<?> insertTempCourse(/*@AuthenticationPrincipal*/ @RequestParam Long id, @RequestBody ReqTempDTO course) {
        course.setId(id);
        courseService.insertTempCourse(course);

        return null;
    }
}
