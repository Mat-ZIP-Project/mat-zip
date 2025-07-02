package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;
import web.mvc.domain.MeetupParticipant;

import java.time.LocalDateTime;

@Getter @Setter
public class MeetupParticipantResponseDto {
    private Long joinId;
    private String joinStatus;
    private LocalDateTime joinedAt;
    private Long userId;
    private Long meetingId;

    public static MeetupParticipantResponseDto fromEntity(MeetupParticipant p) {
        MeetupParticipantResponseDto dto = new MeetupParticipantResponseDto();
        dto.setJoinId(p.getJoinId());
        dto.setJoinStatus(p.getJoinStatus());
        dto.setJoinedAt(p.getJoinedAt());
        dto.setUserId(p.getUser().getId());
        dto.setMeetingId(p.getMeeting().getMeetingId());
        return dto;
    }
}
