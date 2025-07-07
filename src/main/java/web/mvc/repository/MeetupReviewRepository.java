package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import web.mvc.domain.MeetupReview;

import java.util.List;

@Repository
public interface MeetupReviewRepository extends JpaRepository<MeetupReview, Long> {
    // 특정 모임 참가이력의 리뷰 조회 (1명 당 1건의 리뷰)
    MeetupReview findByMeetupParticipant_JoinId(Long joinId);

    // 특정 모임의 전체 리뷰
    List<MeetupReview> findByMeetupParticipant_Meeting_MeetingId(Long meetingId);

    // 특정 사용자가 작성한 모임 리뷰 조회
    @Query("SELECT mr FROM MeetupReview mr JOIN mr.meetupParticipant mp JOIN mp.user u WHERE u.id = :userId")
    List<MeetupReview> findMeetingReviewsById(@Param("id") Long id);
}
