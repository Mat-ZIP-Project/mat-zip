// web.mvc.service.ReviewService.java
package web.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.*;
import web.mvc.dto.ReviewRequestDto;
import web.mvc.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PointLogRepository pointLogRepository;

    /**
     * 리뷰 작성
     *  - 리뷰 작성 시 100P 적립
     *  - 영수증 유효기간 7일 이내만 가능(visitDate + 7일 >= 오늘)
     */
    @Transactional
    public void writeReview(ReviewRequestDto dto) {
        // 1. 중복 리뷰 방지: 동일 날짜 + 식당 + 사용자
        boolean isDuplicate = reviewRepository.existsByUser_IdAndRestaurant_RestaurantIdAndVisitDate(
                dto.getUserId(), dto.getRestaurantId(), dto.getVisitDate());
        if (isDuplicate) {
            throw new IllegalStateException("해당 날짜에는 이미 리뷰를 작성하셨습니다.");
        }

        // 2. 예약 리뷰(현장 방문은 제외)라면 예약 이력 체크
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

        PointLog pointLog = PointLog.builder()
                .isEarned(true)
                .amount(100)
                .pointLog(after)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        pointLogRepository.save(pointLog);
    }
}
