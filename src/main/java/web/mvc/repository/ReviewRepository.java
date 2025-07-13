package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.mvc.domain.Restaurant;
import web.mvc.domain.Review;
import web.mvc.dto.ReviewSummaryDto;
import web.mvc.domain.User;
import web.mvc.exception.BasicException;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /** 특정 식당에 대한 리뷰 리스트 조회 (이미지포함) */
    @Query("SELECT r FROM Review r JOIN FETCH r.reviewImages WHERE r.restaurant.restaurantId = :restId")
    List<Review> findByRestaurantWithImages(@Param("restId") Long restId);

    /** (식당 통계용) 최근 일간 총 리뷰 수 및 현지인 리뷰 수 */
    @Query("""
      SELECT new web.mvc.dto.ReviewSummaryDto(
        COUNT(r),
        SUM(CASE WHEN r.localReview = true THEN 1 ELSE 0 END)
      )
      FROM Review r
      WHERE r.restaurant.id = :restaurantId
        AND r.visitDate BETWEEN :from AND :to
    """)
    ReviewSummaryDto findReviewSummaryByVisitDate(@Param("restaurantId") Long restaurantId,
                                                  @Param("from")   LocalDate from,
                                                  @Param("to")     LocalDate to);


}
