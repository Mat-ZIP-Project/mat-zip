package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.ReviewRequestDto;
import web.mvc.service.ReviewService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 등록(작성)
    @PostMapping("/write")
    public ResponseEntity<String> writeReview(@RequestBody ReviewRequestDto dto) {
        try {
            reviewService.writeReview(dto);
            return ResponseEntity.ok("리뷰 작성을 완료했습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("리뷰 작성에 실패했습니다.: " + e.getMessage());
        }
    }

    // 리뷰 삭제 기능 추가
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok("리뷰 삭제를 완료했습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("리뷰 삭제에 실패했습니다.: " + e.getMessage());
        }
    }

    // 리뷰 수정 기능 추가
    @PutMapping("/{reviewId}")
    public ResponseEntity<String> updateReview(@PathVariable Long reviewId, @RequestBody ReviewRequestDto dto) {
        try {
            reviewService.updateReview(reviewId, dto);
            return ResponseEntity.ok("리뷰 수정을 완료했습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("리뷰 수정에 실패했습니다.: " + e.getMessage());
        }
    }
}
