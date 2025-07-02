package web.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.MeetupParticipant;
import web.mvc.domain.MeetupReview;
import web.mvc.dto.MeetupReviewRequestDto;
import web.mvc.repository.MeetupParticipantRepository;
import web.mvc.repository.MeetupReviewRepository;

@Service
@RequiredArgsConstructor
public class MeetupReviewServiceImpl implements MeetupReviewService {

    private final MeetupReviewRepository meetupReviewRepository;
    private final MeetupParticipantRepository meetupParticipantRepository;

    @Override
    @Transactional
    public MeetupReview createReview(MeetupReviewRequestDto dto) {
        MeetupParticipant participant = meetupParticipantRepository.findById(dto.getJoinId())
                .orElseThrow(() -> new IllegalArgumentException("참가 이력이 없습니다."));
        // 중복 후기 방지
        if (meetupReviewRepository.findByMeetupParticipant_JoinId(dto.getJoinId()) != null) {
            throw new IllegalStateException("이미 후기가 작성된 이력입니다.");
        }
        MeetupReview review = MeetupReview.builder()
                .meetupParticipant(participant)
                .reviewContent(dto.getReviewContent())
                .imageUrl(dto.getImageUrl())
                .build();
        return meetupReviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetupReview getReviewByJoinId(Long joinId) {
        MeetupReview review = meetupReviewRepository.findByMeetupParticipant_JoinId(joinId);
        if (review == null) throw new IllegalArgumentException("후기가 없습니다.");
        return review;
    }
}
