package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.Reservation;
import web.mvc.domain.ReservationPayment;
import web.mvc.domain.User;

import java.util.Optional;

public interface ReservationPaymentRepository extends JpaRepository<ReservationPayment,Long> {
    // 특정 예약에 대한 결제 정보 조회
    Optional<ReservationPayment> findByReservation(Reservation reservation);
    // merchant_uid로 결제 정보 조회
    Optional<ReservationPayment> findByMerchantUid(String merchantUid);
    // imp_uid로 결제 정보 조회
    Optional<ReservationPayment> findByImpUid(String impUid);
}
