package web.mvc.service;

import web.mvc.domain.MeetupReview;
import web.mvc.dto.MeetupReviewRequestDto;

public interface MeetupReviewService {
    MeetupReview createReview(MeetupReviewRequestDto dto);
    MeetupReview getReviewByJoinId(Long joinId);
}
