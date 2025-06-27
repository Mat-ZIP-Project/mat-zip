package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 기존 메서드 (예약 존재 여부만 확인 가능)
    boolean existsByUser_UserIdAndRestaurant_RestaurantIdAndStatus(String userUserId, Long restaurantRestaurantId, String status);
    boolean existsByUser_IdAndRestaurant_RestaurantIdAndStatus(Long userId, Long restaurantRestaurantId, String status);

    // 영수증 인증된 예약자만 조회 (리뷰 작성 조건용)
    boolean existsByUser_IdAndRestaurant_RestaurantIdAndStatusAndReceiptVerifiedTrue(Long userId, Long restaurantId, String status);
}