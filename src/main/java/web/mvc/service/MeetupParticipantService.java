package web.mvc.service;

import web.mvc.domain.MeetupParticipant;
import web.mvc.dto.MeetupParticipantRequestDto;
import java.util.List;

public interface MeetupParticipantService {
    MeetupParticipant joinMeeting(MeetupParticipantRequestDto dto);
    void cancelJoin(Long joinId);
    List<MeetupParticipant> getParticipantsByMeeting(Long meetingId);
    List<MeetupParticipant> getMyJoinedMeetings(Long userId);
}
