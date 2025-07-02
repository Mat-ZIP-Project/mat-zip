package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MeetupParticipantRequestDto {
    private Long meetingId;
    private Long userId;
}
