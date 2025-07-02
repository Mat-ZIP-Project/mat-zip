package web.mvc.service;

import web.mvc.domain.Meeting;
import web.mvc.dto.MeetingRequestDto;
import java.util.List;

public interface MeetingService {
    Meeting createMeeting(MeetingRequestDto dto);
    List<Meeting> getAllMeetings();
    Meeting getMeetingById(Long meetingId);
    Meeting updateMeeting(Long meetingId, MeetingRequestDto dto);
    void deleteMeeting(Long meetingId);
}
