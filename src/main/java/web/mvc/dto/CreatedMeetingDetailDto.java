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
public class CreatedMeetingDetailDto {
    private Long meetingId;
    private String title;
    private String description;
    private int maxParticipants;
    private int currentParticipants;
    private LocalDateTime meetingTime;
    private LocalDateTime createdAt;

    // 연관된 식당 정보
    private Long restaurantId;
    private String restaurantName;
}
