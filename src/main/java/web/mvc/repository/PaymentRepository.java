package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.Reservation;
import web.mvc.domain.Payment;
import web.mvc.util.Enums;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    // 특정 예약에 대한 결제 정보 조회
    Optional<Payment> findByReservation(Reservation reservation);
    // merchant_uid로 결제 정보 조회
    Optional<Payment> findByMerchantUid(String merchantUid);
    // imp_uid로 결제 정보 조회 ( 결제 취소 시 impUid로 찾을 때 사용)
    Optional<Payment> findByImpUid(String impUid);

    Optional<Payment> findByReservationAndStatus(Reservation reservation, Enums.PaymentStatus status);
}
