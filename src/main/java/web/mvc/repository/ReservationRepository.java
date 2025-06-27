package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.mvc.domain.Reservation;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    // 특정 사용자의 모든 예약 조회
    List<Reservation> findByUser(User user);

    // 특정 식당의 모든 예약 조회
    List<Reservation> findByReservationId(Long reservationId);

    // 특정 식당의 예약 조회
    Optional<Reservation> findById(Long reservationId);
}
