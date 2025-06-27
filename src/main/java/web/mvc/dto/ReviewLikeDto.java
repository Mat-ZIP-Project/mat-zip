package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewLikeDto {
    private Long userId;
    private Long reviewId;
}
