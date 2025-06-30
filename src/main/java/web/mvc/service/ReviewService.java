package web.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import web.mvc.domain.*;
import web.mvc.dto.ReviewRequestDto;
import web.mvc.repository.*;

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

        // 2. 예약 리뷰(현장 방문은 제외)인 경우, 예약 이력 체크
        if (!dto.isSiteReview()) {
            boolean hasReservation = reservationRepository
                    .existsByUser_IdAndRestaurant_RestaurantIdAndStatus(
                            dto.getUserId(), dto.getRestaurantId(), "예약 완료");
            if (!hasReservation) {
                throw new IllegalStateException("예약 방문 이력이 확인되지 않았습니다.");
            }
        }

        // 회원과 식당 객체를 엔티티에서 검증후 연관관계 확인해서 리뷰 저장
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 회원과 식당 객체를 엔티티에서 검증후 연관관계 확인해서 리뷰 저장
        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("식당 없음"));

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
