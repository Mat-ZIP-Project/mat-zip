package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewReportDto {
    private Long userId;
    private Long reviewId;
    private String reason;
}
