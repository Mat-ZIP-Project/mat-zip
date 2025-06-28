package web.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    public void writeReview(ReviewRequestDto dto) {
        // 1. 중복 리뷰 방지: 동일 날짜 + 식당 + 사용자
        boolean isDuplicate = reviewRepository.existsByUser_IdAndRestaurant_RestaurantIdAndVisitDate(
                dto.getUserId(), dto.getRestaurantId(), dto.getVisitDate());
        if (isDuplicate) {
            throw new IllegalStateException("해당 날짜에는 이미 리뷰를 작성하셨습니다.");
        }

        // 2. 예약 및 영수증 인증 여부 확인 (siteReview == false인 경우에만)
        if (!dto.isSiteReview()) {
            boolean hasReservation = reservationRepository
                    .existsByUser_IdAndRestaurant_RestaurantIdAndStatusAndReceiptVerifiedTrue(
                            dto.getUserId(), dto.getRestaurantId(), "예약 완료");

            if (!hasReservation) {
                throw new IllegalStateException("예약 방문 이력이 확인되지 않았습니다.");
            }
        }

        // 3. 사용자, 식당 객체 조회
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("식당 없음"));

        // 4. Review 엔티티 생성 및 저장
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
    }
}