package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.mvc.domain.MeetupParticipant;

import java.util.List;

@Repository
public interface MeetupParticipantRepository extends JpaRepository<MeetupParticipant, Long> {

    // 특정 모임의 참가자 전체 조회
    List<MeetupParticipant> findByMeeting_MeetingId(Long meetingId);

    // 특정 사용자의 참가 이력 전체 조회
    List<MeetupParticipant> findByUser_Id(Long userId);

    // 하나의 모임-사용자 중복참여 방지 체크 (사용자 → 모임 순)
    boolean existsByUser_IdAndMeeting_MeetingId(Long userId, Long meetingId);

}
