package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
public class MeetingRequestDto {
    private String title;
    private String description;
    private int maxParticipants;
    private LocalDateTime meetingTime;
    private Long userId;           // 개설자(회원)
    private Long restaurantId;     // 장소(식당)
}
