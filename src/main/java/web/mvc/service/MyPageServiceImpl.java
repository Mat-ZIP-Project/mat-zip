package web.mvc.service;

import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.Reservation;
import web.mvc.domain.ReservationPayment;
import web.mvc.domain.Review;
import web.mvc.dto.ReservationDetailDto;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.ReservationPaymentRepository;
import web.mvc.repository.ReservationRepository;
import web.mvc.repository.ReviewRepository;
import web.mvc.repository.UserRepository;
import web.mvc.util.Enums;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageServiceImpl implements MyPageService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentService paymentService;
    // 모임 rep 주입 필요
    private final ReservationPaymentRepository reservationPaymentRepository;

    /**
     *  사용자 전체 예약 내역 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReservationDetailDto> getUserReservations(Long id) throws BasicException {

        List<Reservation> reservations = reservationRepository.findByUserIdAndStatus(id, Enums.ReservationStatus.APPROVED.name());

        // 조회된 예약 리스트를 ReservationDetailDto로 변환
        return reservations.stream()
                .map(reservation -> {
                    String paymentStatus;
                    Optional<ReservationPayment> paymentOpt = reservationPaymentRepository.findByReservation(reservation);
                    if (paymentOpt.isPresent()) {
                        // 결제 정보가 존재하면 해당 결제 상태를 사용
                        paymentStatus = String.valueOf(paymentOpt.get().getStatus());
                    } else {
                        // 예약이 'CONFIRMED' 상태로 조회되었으므로, 결제 정보가 명시적으로 없어도
                        // 논리적으로는 결제 완료된 것으로 간주 (혹은 외부 시스템에서 처리되었거나 정보 누락)
                        paymentStatus = "결제 완료 (정보 없음)";
                    }
                    return ReservationDetailDto.builder()
                            .reservationId(reservation.getReservationId())
                            .restaurantName(reservation.getRestaurant() != null ? reservation.getRestaurant().getRestaurantName() : "알 수 없음")
                            .date(LocalDateTime.parse(reservation.getDate()))
                            .time(LocalDateTime.parse(reservation.getTime()))
                            .numPeople(reservation.getNumPeople())
                            .status(reservation.getStatus())
                            .ownerNotes(reservation.getOwnerNotes())
                            .createdAt(reservation.getCreatedAt())
                            .paymentStatus(paymentStatus)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     *  사용자 전체 리뷰 내역
     */
    @Override
    @Transactional(readOnly = true)
    public List<Review> getUserReviews(Long id) throws BasicException {

        List<Review> reviews = reviewRepository.findByUserId(id);
        return reviews;
    }

    /**
     *  마이페이지에서 사용자가 예약 취소
     */
    @Override
    public ReservationDetailDto cancelReservation(Long id, Long reservationId) throws BasicException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESERVATION_NOT_FOUND));

        // 예약자가 같은지 비교
        if (!reservation.getUser().getId().equals(id)) {
            throw new BasicException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 예약 상태 확인 (취소 가능한지)
        String currentStatus = reservation.getStatus();
        // APPROVED, PENDING_APPROVAL 상태만 취소가 가능함.
        if (!(currentStatus.equals(Enums.ReservationStatus.APPROVED.name())
        || currentStatus.equals(Enums.ReservationStatus.PENDING_APPROVAL.name()))) {
            throw new BasicException(ErrorCode.INVALID_RESERVATION_STATUS);
        }
        // 시간 기반 취소 가능여부
        LocalDate reservationDate = LocalDate.parse(reservation.getDate());
        LocalTime reservationTime = LocalTime.parse(reservation.getTime());
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime);
        LocalDateTime now = LocalDateTime.now();

        // 예약 시간이 현재 시간보다 이전이면 취소 불가능
        if (reservationDateTime.isBefore(now)) {
            throw new BasicException(ErrorCode.INVALID_RESERVATION_STATUS);
        }
        // 예약 시간이 3시간 이내로 남았으므로 취소 불가능
        long hoursUntilReservation = ChronoUnit.HOURS.between(now, reservationDate);
        if (hoursUntilReservation < 3) {
            throw new BasicException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        // 결제 정보 확인 및 PaymentService를 통한 환불 처리
        Optional<ReservationPayment> paymentOpt = reservationPaymentRepository.findByReservation(reservation);
        String finalPaymentStatus = "";

        if (paymentOpt.isPresent()) {
            ReservationPayment payment = paymentOpt.get();
            // PAID 상태의 결제만 환불 시도
            if (payment.getStatus().equals(Enums.PaymentStatus.PAID.name())) {
                try {
                    paymentService.cancelPayment(
                            payment.getImpUid(),
                            new BigDecimal(payment.getAmount()), // ReservationPayment의 amount를 사용
                            "사용자 요청에 의한 예약 취소 환불"
                    );

                    // 환불 성공 시 결제 상태 업데이트 (PaymentService에서도 하지만 여기서 한 번 더 명시적으로)
                    payment.setStatus(Enums.PaymentStatus.valueOf(Enums.PaymentStatus.CANCELLED.name()));
                    reservationPaymentRepository.save(payment);
                    log.info("예약 ID '{}' 결제 ID '{}'에 대한 환불 성공 및 결제 상태 '{}'로 업데이트.", reservationId, payment.getPaymentId(), payment.getStatus());
                    finalPaymentStatus = String.valueOf(payment.getStatus()); // 실제 환불된 상태
                } catch (IamportResponseException | IOException e) {
                    throw new BasicException(ErrorCode.PAYMENT_CANCEL_FAILED);
                } catch (BasicException e) {
                    throw e; // BasicException은 그대로 다시 던집니다.
                } catch (Exception e) {
                    throw new BasicException(ErrorCode.PAYMENT_CANCEL_FAILED);
                }
            } else {
                log.warn("예약 ID '{}'의 결제는 PAID 상태가 아니므로 환불을 시도하지 않습니다. 현재 상태: {}", reservationId, payment.getStatus());
                finalPaymentStatus = String.valueOf(payment.getStatus()); // 기존 결제 상태 유지
            }
        } else {
            // 결제 정보가 없는 예약 (예: 현장 결제만 가능)
            log.warn("예약 ID '{}'에 대한 결제 정보가 없습니다. 환불 없이 예약만 취소 처리합니다.", reservationId);
        }

        // 예약 상태 변경
        reservation.setStatus(Enums.ReservationStatus.CANCELLED.name());
        reservationRepository.save(reservation);

        // 취소된 예약 정보를 DTO로 변환하여 반환
        return ReservationDetailDto.builder()
                .reservationId(reservation.getReservationId())
                .restaurantName(reservation.getRestaurant().getRestaurantName())
                .date(LocalDateTime.parse(reservation.getDate()))
                .time(LocalDateTime.parse(reservation.getTime()))
                .numPeople(reservation.getNumPeople())
                .status(reservation.getStatus())
                .ownerNotes(reservation.getOwnerNotes())
                .createdAt(reservation.getCreatedAt())
                .paymentStatus(finalPaymentStatus)
                .build();

    }
}
