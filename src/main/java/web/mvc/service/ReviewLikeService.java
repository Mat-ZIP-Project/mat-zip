package web.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.ReviewLike;
import web.mvc.dto.ReviewLikeDto;
import web.mvc.repository.ReviewLikeRepository;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {

    private final ReviewLikeRepository reviewLikeRepository;

    @Transactional
    public String toggleReviewLike(ReviewLikeDto dto) {
        boolean alreadyLiked = reviewLikeRepository.existsByUserIdAndReviewId(dto.getUserId(), dto.getReviewId());

        if (alreadyLiked) {
            reviewLikeRepository.deleteByUserIdAndReviewId(dto.getUserId(), dto.getReviewId());
            return "좋아요를 취소합니다.";
        } else {
            ReviewLike reviewLike = new ReviewLike();
            reviewLike.setUserId(dto.getUserId());
            reviewLike.setReviewId(dto.getReviewId());
            reviewLikeRepository.save(reviewLike);
            return "해당 리뷰를 좋아합니다.";
        }
    }
}
