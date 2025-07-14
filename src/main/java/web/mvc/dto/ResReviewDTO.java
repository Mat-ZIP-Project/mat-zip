package web.mvc.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ResReviewDTO {
    private Long reviewId;
    private String content;
    private int rating;
    private LocalDateTime reviewedAt;
    private LocalDate visitDate;
    private boolean localReview;
    private String userNickname; // 유저 정보에서 보여줄 닉네임
    private List<String> imageUrls; // 리뷰 이미지 URL들
}
