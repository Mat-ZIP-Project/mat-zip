package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import web.mvc.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long> {

    // 특정 사용자의 모든 예약 조회
    List<Reservation> findByUserIdAndStatus(Long id, String status);

    List<Reservation> findByUserIdAndStatusIn(Long id, List<String> status);

    List<Reservation> findByUserIdAndStatusIsNot(Long id, String status);

    // 특정 식당의 예약 조회
    Optional<Reservation> findByReservationId(Long reservationId);
    // 기존 메서드 (예약 존재 여부만 확인 가능)
    boolean existsByUser_UserIdAndRestaurant_RestaurantIdAndStatus(String userUserId, Long restaurantRestaurantId, String status);
    boolean existsByUser_IdAndRestaurant_RestaurantIdAndStatus(Long userId, Long restaurantRestaurantId, String status);

    /**
     *  예약 알림을 보낼 대상을 조회하는 메서드
     *  상태가 'approved'이고, 아직 알림을 보내지 않은.
     *  현재 날짜에 해당하는 모든 예약 찾는다.
     *  스프링 스케줄러 필요한 것
     */
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.status = 'APPROVED' " +
            "AND r.reminded = false " + // 아직 알림을 보내지 않은 경우
            "AND r.date = :currentDate")
    List<Reservation> findReservationsForReminder(@Param("currentDate") String currentDate);
}
