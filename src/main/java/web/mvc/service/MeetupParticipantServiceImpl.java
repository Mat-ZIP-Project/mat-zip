package web.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.Meeting;
import web.mvc.domain.MeetupParticipant;
import web.mvc.domain.User;
import web.mvc.dto.MeetupParticipantRequestDto;
import web.mvc.repository.MeetingRepository;
import web.mvc.repository.MeetupParticipantRepository;
import web.mvc.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetupParticipantServiceImpl implements MeetupParticipantService {

    private final MeetupParticipantRepository meetupParticipantRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MeetupParticipant joinMeeting(MeetupParticipantRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        Meeting meeting = meetingRepository.findById(dto.getMeetingId())
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다."));
        // 중복참여 방지
        if (meetupParticipantRepository.existsByUser_IdAndMeeting_MeetingId(dto.getUserId(), dto.getMeetingId())) {
            throw new IllegalStateException("이미 참가한 모임입니다.");
        }
        MeetupParticipant participant = MeetupParticipant.builder()
                .meeting(meeting)
                .user(user)
                .build();
        return meetupParticipantRepository.save(participant);
    }

    @Override
    @Transactional
    public void cancelJoin(Long joinId) {
        meetupParticipantRepository.deleteById(joinId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetupParticipant> getParticipantsByMeeting(Long meetingId) {
        return meetupParticipantRepository.findByMeeting_MeetingId(meetingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetupParticipant> getMyJoinedMeetings(Long userId) {
        return meetupParticipantRepository.findByUser_Id(userId);
    }
}
