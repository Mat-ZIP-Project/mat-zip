package web.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.*;
import web.mvc.dto.ReviewRequestDto;
import web.mvc.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PointRepository pointRepository;

    /**
     * 리뷰 작성
     *  - 리뷰 작성 시 100P 적립
     *  - 영수증 유효기간 7일 이내만 가능(visitDate + 7일 >= 오늘)
     */
    @Transactional
    public void writeReview(ReviewRequestDto dto) {
        // 1. 중복 리뷰 방지
        boolean isDuplicate = reviewRepository.existsByUser_IdAndRestaurant_RestaurantIdAndVisitDate(
                dto.getUserId(), dto.getRestaurantId(), dto.getVisitDate());
        if (isDuplicate) {
            throw new IllegalStateException("해당 날짜에는 이미 리뷰를 작성하셨습니다.");
        }

        // 2. 예약 리뷰라면 예약 이력 체크
        if (!dto.isSiteReview()) {
            boolean hasReservation = reservationRepository
                    .existsByUser_IdAndRestaurant_RestaurantIdAndStatus(
                            dto.getUserId(), dto.getRestaurantId(), "예약 완료");
            if (!hasReservation) {
                throw new IllegalStateException("예약 방문 이력이 확인되지 않았습니다.");
            }
        }

        // 3. 영수증 유효기간(visitDate ~ 오늘-7일 사이) 체크
        LocalDate visitDate = dto.getVisitDate();
        LocalDate now = LocalDate.now();
        if (visitDate.isBefore(now.minusDays(7)) || visitDate.isAfter(now)) {
            throw new IllegalStateException("영수증(방문일자)은 최근 7일 이내만 리뷰 작성이 가능합니다.");
        }

        // 4. 사용자, 식당 조회
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("식당 정보가 없습니다."));

        // 5. 리뷰 저장
        Review review = Review.builder()
                .content(dto.getContent())
                .rating(dto.getRating())
                .category(dto.getCategory())
                .reviewedAt(LocalDateTime.now())
                .visitDate(dto.getVisitDate())
                .siteReview(dto.isSiteReview())
                .sourceId(dto.getSourceId())
                .user(user)
                .restaurant(restaurant)
                .build();
        reviewRepository.save(review);

        // 6. 100P 적립 - users & point_logs
        int before = user.getPointBalance() != null ? user.getPointBalance() : 0;
        int after = before + 100;
        user.setPointBalance(after);
        userRepository.save(user);

        // Point 로그 적립
        Point pointLog = Point.builder()
                .isEarned("적립")              // "적립", "사용", "취소"
                .pointAmount(100)              // 적립 금액
                .pointLog(after)               // 적립 후 잔액
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        pointRepository.save(pointLog);
    }

    /** 리뷰 삭제 */
    @Transactional
    public void deleteReview(Long reviewId) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            throw new IllegalStateException("해당 리뷰를 찾을 수 없습니다.");
        }
        reviewRepository.deleteById(reviewId);
    }

    /** 리뷰 수정 */
    @Transactional
    public void updateReview(Long reviewId, ReviewRequestDto dto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalStateException("해당 리뷰를 찾을 수 없습니다."));
        if (dto.getContent() != null) review.setContent(dto.getContent());
        if (dto.getRating() != null) review.setRating(dto.getRating());
        review.setReviewedAt(LocalDateTime.now()); // 수정시 수정일 갱신
        reviewRepository.save(review);
    }
}
