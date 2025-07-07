package web.mvc.service;

import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.*;
import web.mvc.dto.ReservationDetailDto;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.*;
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
    private final PointRepository pointRepository;

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
    public void cancelReservation(Long id, Long reservationId) throws BasicException {
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
                            new BigDecimal(payment.getOriginalAmount()), // ReservationPayment의 amount를 사용
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
        User user = reservation.getUser();

        // ✅ 포인트 회수 로직 추가 (취소 시)
        if (reservation.isPointsAwarded()) { // 포인트가 지급된 상태라면 회수
            int refundedPoints = 200;
            int currentPointBalance = (user.getPointBalance() != null) ? user.getPointBalance() : 0;
            int newPointBalance = currentPointBalance - refundedPoints;

            if (newPointBalance < 0) newPointBalance = 0; // 잔액이 마이너스가 되지 않도록 방지

            user.setPointBalance(newPointBalance); // 사용자 포인트 잔액 업데이트
            userRepository.save(user); // 사용자 정보 저장

            // 포인트 로그 기록 (사용/회수)
            Point pointLog = Point.builder()
                    .user(user)
                    .isEarned("취소") // 사용 (false)
                    .pointAmount(refundedPoints) // 회수된 금액
                    .createdAt(LocalDateTime.now())
                    .pointLog(newPointBalance) // 이 시점의 총 포인트 잔액
                    .build();
            pointRepository.save(pointLog); // 포인트 로그 저장

            reservation.setPointsAwarded(false); // 포인트 회수 완료 표시
            log.info("사용자 ID '{}'에게 예약 ID '{}' 취소로 {} 포인트 회수. 현재 포인트: {}",
                    user.getUserId(), reservationId, refundedPoints, newPointBalance);
        } else {
            log.info("예약 ID '{}'는 포인트가 지급되지 않았으므로 회수할 포인트가 없습니다.", reservationId);
        }

        reservationRepository.save(reservation);
        log.info("예약 ID '{}'가 '{}' 상태로 취소 완료.", reservationId, reservation.getStatus());
    }

    /**
     *  사용자의 등급을 확인하고 필요시 업데이트합니다.
     */
    @Override
    public void checkAndUpdateUserGrade(User user) throws BasicException {
        String currentGrade = user.getUserGrade();

       int maxPoints = pointRepository.findMaxPointLogByUser(user);

       String newGrade = currentGrade;

       // 등급 상승 로직
        if ("브론즈".equals(currentGrade) && maxPoints >= 3000) {
            newGrade = "실버";
        } else if ("실버".equals(currentGrade) && maxPoints >= 10000) {
            newGrade = "먹짱";
        }

        if (!currentGrade.equals(newGrade)) {
            user.setUserGrade(newGrade);
            userRepository.save(user);
            log.info("등급이 상승하였습니다.");
        } else {
            log.info("등급이 변경되지 않았습니다.");
        }
    }
}
