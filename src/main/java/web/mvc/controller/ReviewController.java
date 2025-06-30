package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.ReviewRequestDto;
import web.mvc.service.ReviewService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class  ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/write")
    public ResponseEntity<String> writeReview(@RequestBody ReviewRequestDto dto) {
        try {
            reviewService.writeReview(dto);
            return ResponseEntity.ok("리뷰 작성을 완료했습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("리뷰 작성에 실패했습니다.: " + e.getMessage());
        }
    }
}