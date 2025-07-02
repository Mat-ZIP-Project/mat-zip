package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;
import web.mvc.domain.Meeting;

import java.time.LocalDateTime;

@Getter @Setter
public class MeetingResponseDto {
    private Long meetingId;
    private String title;
    private String description;
    private int maxParticipants;
    private int currentParticipants;
    private LocalDateTime meetingTime;
    private LocalDateTime createdAt;
    private Long userId;
    private Long restaurantId;

    public static MeetingResponseDto fromEntity(Meeting meeting) {
        MeetingResponseDto dto = new MeetingResponseDto();
        dto.setMeetingId(meeting.getMeetingId());
        dto.setTitle(meeting.getTitle());
        dto.setDescription(meeting.getDescription());
        dto.setMaxParticipants(meeting.getMaxParticipants());
        dto.setCurrentParticipants(meeting.getCurrentParticipants());
        dto.setMeetingTime(meeting.getMeetingTime());
        dto.setCreatedAt(meeting.getCreatedAt());
        dto.setUserId(meeting.getUser().getId());
        dto.setRestaurantId(meeting.getRestaurant().getRestaurantId());
        return dto;
    }
}
