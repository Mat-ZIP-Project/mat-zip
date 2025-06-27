package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.Review;
import java.time.LocalDate;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUser_IdAndRestaurant_RestaurantIdAndVisitDate(Long userId, Long restaurantId, LocalDate visitDate);
}