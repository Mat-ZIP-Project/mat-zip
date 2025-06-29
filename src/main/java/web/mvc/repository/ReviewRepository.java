package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.Review;

import java.time.LocalDate;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUser_IdAndRestaurant_RestaurantIdAndVisitDate(Long userId, Long restaurantId, LocalDate visitDate);

    /**
     * 사용자가 작성한 리뷰 내역
     */
    List<Review> findByUserId(Long id);
}