package web.mvc.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 상세정보 응답
 * */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailResponse {
    private Long reviewId;
    private String content;
    private int rating;
    private LocalDateTime reviewedAt;
    private boolean localReview;
    private LocalDate visitDate;
    private String restaurantName;

    private List<String> imageNames;
}
