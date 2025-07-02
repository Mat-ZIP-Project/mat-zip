package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MeetupReviewRequestDto {
    private Long joinId;      // 참가 이력 PK
    private String reviewContent;
    private String imageUrl;  // 첨부 이미지
}
