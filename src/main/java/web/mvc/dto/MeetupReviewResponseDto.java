package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;
import web.mvc.domain.MeetupReview;

import java.time.LocalDateTime;

@Getter @Setter
public class MeetupReviewResponseDto {
    private Long meetupReviewId;
    private String reviewContent;
    private String imageUrl;
    private LocalDateTime createdAt;
    private Long joinId;

    public static MeetupReviewResponseDto fromEntity(MeetupReview review) {
        MeetupReviewResponseDto dto = new MeetupReviewResponseDto();
        dto.setMeetupReviewId(review.getMeetupReviewId());
        dto.setReviewContent(review.getReviewContent());
        dto.setImageUrl(review.getImageUrl());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setJoinId(review.getMeetupParticipant().getJoinId());
        return dto;
    }
}
