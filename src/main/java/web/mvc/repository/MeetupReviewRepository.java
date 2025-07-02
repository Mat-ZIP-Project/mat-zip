package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.mvc.domain.MeetupReview;

import java.util.List;

@Repository
public interface MeetupReviewRepository extends JpaRepository<MeetupReview, Long> {
    // 특정 모임 참가이력의 리뷰 조회 (1명 당 1건의 리뷰)
    MeetupReview findByMeetupParticipant_JoinId(Long joinId);

    // 특정 모임의 전체 리뷰
    List<MeetupReview> findByMeetupParticipant_Meeting_MeetingId(Long meetingId);
}
