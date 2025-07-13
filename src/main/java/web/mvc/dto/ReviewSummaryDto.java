package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
/**
 * 식당 사장 리뷰 건 통계차트용
 */
public class ReviewSummaryDto {
    private long totalReviews;
    private long localReviews;
}
