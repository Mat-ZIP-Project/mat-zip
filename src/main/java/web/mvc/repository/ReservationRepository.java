package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.Reservation;
import web.mvc.domain.User;
import web.mvc.util.Enums;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    // 특정 사용자의 모든 예약 조회
    List<Reservation> findByUser(User user);

    // 특정 식당의 모든 예약 조회
    List<Reservation> findByReservationId(Long reservationId);

    // 특정 상태의 예약 조회
    Optional<Reservation> findyByReservationIdAndStatus(Long reservationId, Enums.ReservationStatus status);
}
