package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class ReviewRequestDto {
    private Long userId;
    private Long restaurantId;
    private String content;
    private int rating;
    private String category;
    private Long sourceId;
    private LocalDate visitDate;
    private boolean siteReview;
}
