package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.Restaurant;
import web.mvc.domain.Review;

import java.time.LocalDate;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUser_IdAndRestaurant_RestaurantIdAndVisitDate(Long userId, Long restaurantId, LocalDate visitDate);

    /**
     * 사용자가 작성한 리뷰 내역
     */
    List<Review> findByUserId(Long id);

    long countReviewByRestaurant(Restaurant restaurant);

    //  유저가 특정 식당에 쓴 모든 리뷰(중복체크·수정·삭제에 활용)
    List<Review> findByUser_IdAndRestaurant_RestaurantId(Long userId, Long restaurantId);

    //  식당별 전체 리뷰 목록
    List<Review> findByRestaurant(Restaurant restaurant);
}