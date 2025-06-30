package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.mvc.domain.Reservation;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    // 특정 사용자의 모든 예약 조회
//    List<Reservation> findByUser(User user);
//
//    // 특정 식당의 모든 예약 조회
//    List<Reservation> findByReservationId(Long reservationId);
//
//    // 특정 식당의 예약 조회
//    Optional<Reservation> findById(Long reservationId);
    // 기존 메서드 (예약 존재 여부만 확인 가능)
    boolean existsByUser_UserIdAndRestaurant_RestaurantIdAndStatus(String userUserId, Long restaurantRestaurantId, String status);
    boolean existsByUser_IdAndRestaurant_RestaurantIdAndStatus(Long userId, Long restaurantRestaurantId, String status);

    // 영수증 인증된 예약자만 조회 (리뷰 작성 조건용)
    boolean existsByUser_IdAndRestaurant_RestaurantIdAndStatusAndReceiptVerifiedTrue(Long userId, Long restaurantId, String status);
}
