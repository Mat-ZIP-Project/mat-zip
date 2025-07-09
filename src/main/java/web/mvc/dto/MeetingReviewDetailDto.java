package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingReviewDetailDto {
    private Long meetupReviewId;
    private String reviewContent;
    private String imageUrl;
    private LocalDateTime createdAt;

    // 모임 정보
    private Long meetingId;
    private String meetingTitle;
    private String restaurantName;
}
