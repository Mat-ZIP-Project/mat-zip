package web.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.Meeting;
import web.mvc.domain.User;
import web.mvc.domain.Restaurant;
import web.mvc.domain.Point;
import web.mvc.dto.MeetingRequestDto;
import web.mvc.repository.MeetingRepository;
import web.mvc.repository.UserRepository;
import web.mvc.repository.RestaurantRepository;
import web.mvc.repository.PointRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PointRepository pointRepository;

    @Override
    @Transactional
    public Meeting createMeeting(MeetingRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));
        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("식당 정보가 없습니다."));

        // 1. 현지인 인증 여부 체크
        if (user.getGpsVerified() == null || !user.getGpsVerified()) {
            throw new IllegalStateException("현지인 인증 회원만 모임을 개설할 수 있습니다.");
        }

        // 2. 모임 생성 시 500P 차감
        if (user.getPointBalance() == null || user.getPointBalance() < 500) {
            throw new IllegalStateException("마일리지가 부족합니다. [500P 필요]");
        }
        int before = user.getPointBalance();
        int after = before - 500;
        user.setPointBalance(after);
        userRepository.save(user);

        // Point 로그 차감
        Point pointLog = Point.builder()
                .isEarned("사용")                // "적립", "사용", "취소"
                .pointAmount(500)                // 차감 금액 (항상 양수)
                .pointLog(after)                 // 차감 후 잔액
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        pointRepository.save(pointLog);

        // 3. 모임 생성
        Meeting meeting = Meeting.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .maxParticipants(dto.getMaxParticipants())
                .meetingTime(dto.getMeetingTime())
                .user(user)
                .restaurant(restaurant)
                .build();
        return meetingRepository.save(meeting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Meeting getMeetingById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("모임 정보를 찾을 수 없습니다."));
    }

    @Override
    @Transactional
    public Meeting updateMeeting(Long meetingId, MeetingRequestDto dto) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("모임 정보를 찾을 수 없습니다."));
        meeting.setTitle(dto.getTitle());
        meeting.setDescription(dto.getDescription());
        meeting.setMaxParticipants(dto.getMaxParticipants());
        meeting.setMeetingTime(dto.getMeetingTime());

        return meeting;
    }

    @Override
    @Transactional
    public void deleteMeeting(Long meetingId) {
        meetingRepository.deleteById(meetingId);
    }
}
