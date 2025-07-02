package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.mvc.domain.MeetupReview;
import web.mvc.dto.MeetupReviewRequestDto;
import web.mvc.dto.MeetupReviewResponseDto;
import web.mvc.service.MeetupReviewService;

@RestController
@RequestMapping("/api/meetup-reviews")
@RequiredArgsConstructor
public class MeetupReviewController {

    private final MeetupReviewService meetupReviewService;

    // 1. 모임 후기 등록
    @PostMapping
    public ResponseEntity<MeetupReviewResponseDto> createReview(@RequestBody MeetupReviewRequestDto dto) {
        MeetupReview review = meetupReviewService.createReview(dto);
        return ResponseEntity.ok(MeetupReviewResponseDto.fromEntity(review));
    }

    // 2. 모임 후기 조회 (참가 이력 1건당 1개)
    @GetMapping("/participant/{joinId}")
    public ResponseEntity<MeetupReviewResponseDto> getReviewByJoinId(@PathVariable Long joinId) {
        MeetupReview review = meetupReviewService.getReviewByJoinId(joinId);
        return ResponseEntity.ok(MeetupReviewResponseDto.fromEntity(review));
    }
}
