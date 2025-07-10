package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.mvc.domain.Reservation;
import web.mvc.domain.ReservationPayment;
import web.mvc.util.Enums;

import java.util.Optional;

@Repository
public interface ReservationPaymentRepository extends JpaRepository<ReservationPayment,Long> {
    // 특정 예약에 대한 결제 정보 조회
    Optional<ReservationPayment> findByReservation(Reservation reservation);
    // merchant_uid로 결제 정보 조회
    Optional<ReservationPayment> findByMerchantUid(String merchantUid);
    // imp_uid로 결제 정보 조회 ( 결제 취소 시 impUid로 찾을 때 사용)
    Optional<ReservationPayment> findByImpUid(String impUid);

    Optional<ReservationPayment> findByReservationAndStatus(Reservation reservation, String status);
}
