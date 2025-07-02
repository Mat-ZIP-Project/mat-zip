package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.ReqCustomDTO;
import web.mvc.dto.ReqTempDTO;
import web.mvc.dto.ResTempDTO;
import web.mvc.security.CustomUserDetails;
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
    public ResponseEntity<?> searchTempCourse(@AuthenticationPrincipal CustomUserDetails userDetails ) {
        List<ResTempDTO> list = courseService.searchTempCourses(userDetails.getUser().getId());
        return ResponseEntity.ok().body(list);
    }

    /**
     * Temp 코스에 추가
     */
    @PostMapping("/temp")
    public ResponseEntity<?> insertTempCourse(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ReqTempDTO course) {
        course.setUserId(userDetails.getUser().getId());
        return ResponseEntity.ok().body(courseService.insertTempCourse(course));
    }

    /**
     * Temp 코스 변경하기
     */
    @PutMapping("/temp")
    public ResponseEntity<?> updateTempCourse(@AuthenticationPrincipal CustomUserDetails userDetails,@RequestBody List<ReqTempDTO> list) {
        list.get(0).setUserId(userDetails.getUser().getId());
        courseService.updateTempCorse(list);
        return ResponseEntity.ok().build();
    }

    /**
     * Custom 코스 저장
     */
    @PostMapping("/custom")
    public ResponseEntity<?> saveCustomCourse(@AuthenticationPrincipal CustomUserDetails userDetails,@RequestBody List<ReqCustomDTO> list ) {
        list.forEach(dto->dto.setId(userDetails.getUser().getId()));
        return ResponseEntity.ok(courseService.insertCustomCourse(list));
    }
    /**
     * custom 코스 상세보기
     */
    @GetMapping("/custom/details/{couseId}")
    public ResponseEntity<?> searchCustomCourseDetails(@AuthenticationPrincipal CustomUserDetails userDetails,@PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.searchCustomCourse(userDetails.getUser().getId(),courseId));
    }

    /**
     * custom 코스 목록 보기
     */
    @GetMapping("/custom/list")
    public ResponseEntity<?> searchCustomCourseList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(courseService.searchCustomCourseList(userDetails.getUser().getId()));
    }
    /**
     * custom 코스 수정하기
     */
    @PutMapping("/custom/{courseId}")
    public void updateCustomCourse(@PathVariable Long courseId,@RequestBody List<ReqCustomDTO> list) {
        courseService.updateCustomCourse(courseId,list);

    }
    /**
     * cutom 코스 삭제하기
     */
    @DeleteMapping("/custom/{courseId}")
    public void deleteCustomCourse(@PathVariable Long courseId) {
        courseService.deleteCustomCourse(courseId);
    }
}
