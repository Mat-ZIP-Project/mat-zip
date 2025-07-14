package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.Restaurant;
import web.mvc.domain.Review;
import web.mvc.domain.User;
import web.mvc.exception.BasicException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUser_IdAndRestaurant_RestaurantIdAndVisitDate(Long userId, Long restaurantId, LocalDate visitDate);

    /**
     * 사용자가 작성한 리뷰 내역
     */
    List<Review> findByUserId(Long id);

    /**
     *  사용자가 작성한 리뷰 삭제
     */
    Optional<Review> findByReviewIdAndUserId(Long reviewId, Long id) throws BasicException;

    long countReviewByRestaurant(Restaurant restaurant);

    //  유저가 특정 식당에 쓴 모든 리뷰(중복체크·수정·삭제에 활용)
    List<Review> findByUser_IdAndRestaurant_RestaurantId(Long userId, Long restaurantId);

    //  식당별 전체 리뷰 목록
    List<Review> findByRestaurant(Restaurant restaurant);

    // 식당 리뷰 개수 카운트
    int countByRestaurant(Restaurant restaurant);

}