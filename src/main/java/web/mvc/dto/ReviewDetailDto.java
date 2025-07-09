package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailDto {
    private Long reviewId;
    private String content;
    private int rating;
    private LocalDateTime reviewedAt;
    private LocalDate visitDate;
    private String restaurantName;
}
