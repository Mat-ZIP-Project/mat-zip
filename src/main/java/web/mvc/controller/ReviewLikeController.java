package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.ReviewLikeDto;
import web.mvc.service.ReviewLikeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review-likes")
public class ReviewLikeController {

    private final ReviewLikeService reviewLikeService;

    @PostMapping("/toggle")
    public ResponseEntity<String> toggleReviewLike(@RequestBody ReviewLikeDto dto) {
        String result = reviewLikeService.toggleReviewLike(dto);
        return ResponseEntity.ok(result);
    }
}
