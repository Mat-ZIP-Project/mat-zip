package web.mvc.service;

//import com.google.firebase.messaging.Notification;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.*;
import web.mvc.dto.NoShowReservationDto;
import web.mvc.dto.PendingReservationDto;
import web.mvc.dto.ReservationCreateReqDto;
import web.mvc.dto.ReservationCreateResDto;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.*;
import web.mvc.util.Enums;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationPaymentRepository reservationPaymentRepository;
    private final FcmService fcmService;
    private final NotificationRepository notificationRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final PointRepository pointRepository;

    /**
     * 새로운 예약 정보를 생성하는 메서드
     */
    @Override
    @Transactional
    public ReservationCreateResDto createReservation(User user, ReservationCreateReqDto request) throws BasicException {

        Restaurant restaurant = restaurantRepository.findByRestaurantId(request.getRestaurantId())
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        Reservation reservation = Reservation.builder()
                .date(request.getDate())
                .time(request.getTime())
                .numPeople(request.getNumPeople())
                .status("예약 신청")
                .user(user)
                .restaurant(restaurant)
                .createdAt(LocalDateTime.now())
                .build();

        // DB에 저장
        Reservation savedReservation = reservationRepository.save(reservation);

        return new ReservationCreateResDto(savedReservation.getReservationId(), restaurant.getRestaurantName(),"예약이 성공적으로 신청되었습니다.", true);
    }



    /**
     * 특정 예약 상태를 승인( APPROVED ) 또는 거절( REJECTED ) 등으로 업데이트
     * 사장님 메모( ownerNotes )와 처리 시각( approvedAt )을 기록
     * FCM 알림 전송 : FcmService의 메서드를 호출하여 해당 예약 사용자에게 알림을 보낸다.
     * NotificationRepository를 통해 DB에 알림 발송 내역을 저장.
     */

    // 유효한 한글 예약 상태 목록을 직접 정의
    private static final List<String> VALID_STATUSES = Arrays.asList(
            "대기중",                 // PENDING
            "결제 후 사장 승인 대기", // PENDING_APPROVAL
            "예약 완료",             // APPROVED
            "예약 거절",             // REJECTED
            "예약 취소"              // CANCELLED
    );
    // 각 최종 상태 문자열 정의 (비교를 명확히 하기 위해)
    private static final String STATUS_PENDING = "대기중";
    private static final String STATUS_PENDING_APPROVAL = "결제 후 사장 승인 대기";
    private static final String STATUS_APPROVED = "예약 완료";
    private static final String STATUS_REJECTED = "예약 거절";
    private static final String STATUS_CANCELLED = "예약 취소";

    @Override
    @Transactional
    public void updateReservationStatus(Long reservationId, String newStatus, String ownerNotes) throws BasicException {
        Reservation reservation = reservationRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESERVATION_NOT_FOUND));

        String currentStatus = reservation.getStatus();

        // 1. 요청된 상태가 유효한 상태 목록에 있는지 검사
//        if (!VALID_STATUSES.contains(currentStatus)) {
//            log.error("유효하지 않은 예약 상태 요청: 예약 ID '{}', 요청 상태 '{}'", reservationId, currentStatus);
//            throw new BasicException(ErrorCode.RESERVATION_NOT_FOUND); // 에러 코드 세분화
//        }
        // 1. 요청된 newStatus가 유효한지 검사하도록 수정 (기존에는 currentStatus 검사)
        if (!VALID_STATUSES.contains(newStatus)) {  // 수정된 부분
            throw new BasicException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        // 2. 예약 상태 전환 유효성 검사 (비즈니스 규칙 강화)
        // 승인 또는 거절은 PENDING 또는 PENDING_APPROVAL 상태에서만 가능
        if (STATUS_APPROVED.equals(newStatus) || STATUS_REJECTED.equals(newStatus)) {
            if (!(STATUS_PENDING.equals(currentStatus) || STATUS_PENDING_APPROVAL.equals(currentStatus))) {
                log.error("예약 상태 전환 불가: 예약 ID '{}', 현재 상태 '{}' 에서 '{}'로 전환할 수 없음. (승인/거절은 PENDING/PENDING_APPROVAL 에서만 가능)",
                        reservationId, currentStatus, newStatus, STATUS_PENDING_APPROVAL, STATUS_PENDING);
                throw new BasicException(ErrorCode.INVALID_RESERVATION_STATUS);
            }
        }
        // 이미 최종 상태(APPROVED/REJECTED/CANCELLED)인 예약을 PENDING/PENDING_APPROVAL로 되돌리려는 시도 방지
        if (STATUS_PENDING.equals(newStatus) || STATUS_PENDING_APPROVAL.equals(newStatus)) {
            if (STATUS_APPROVED.equals(currentStatus) ||
                    STATUS_REJECTED.equals(currentStatus) ||
                    STATUS_CANCELLED.equals(currentStatus)) {
                log.error("예약 상태 전환 불가: 예약 ID '{}', 이미 최종 상태인 '{}' 에서 '{}'로 되돌릴 수 없음.",
                        reservationId, currentStatus, newStatus);
                throw new BasicException(ErrorCode.INVALID_RESERVATION_STATUS);
            }
        }

        if (currentStatus.equals(newStatus)) {
            return;
        }

        reservation.setStatus(newStatus);
        reservation.setOwnerNotes(ownerNotes);

        User user = reservation.getUser();

        if (STATUS_APPROVED.equals(newStatus)) {
            reservation.setApprovedAt(LocalDateTime.now());

            // 200포인트 적립 로직 추가
            if (!reservation.isPointsAwarded()) {
                int earnedPoints = 200;
                int currentPointBalance = (user.getPointBalance() != null) ? user.getPointBalance() : 0;
                int newPointBalance = currentPointBalance + earnedPoints;
                user.setPointBalance(newPointBalance);
                userRepository.save(user);

                // 포인트 로그 기록
                Point pointLog = Point.builder()
                        .user(user)
                        .isEarned("적립")
                        .pointAmount(earnedPoints)
                        .createdAt(LocalDateTime.now())
                        .pointLog(newPointBalance)
                        .build();
                pointRepository.save(pointLog);

                reservation.setPointsAwarded(true);
                log.info("사용자 ID '{}'에게 예약 ID '{}' 승인으로 {} 포인트 적립. 현재 포인트: {}",
                        user.getUserId(), reservationId, earnedPoints, newPointBalance);

//                myPageService.checkAndUpdateUserGrade(user);
            }
        } else if (STATUS_REJECTED.equals(newStatus)) {
            reservation.setApprovedAt(LocalDateTime.now());

            // ✅ 포인트 회수 로직 추가 (거절 시)
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
                        .isEarned("취소")
                        .pointAmount(refundedPoints) // 회수된 금액
                        .createdAt(LocalDateTime.now())
                        .pointLog(newPointBalance) // 이 시점의 총 포인트 잔액
                        .build();
                pointRepository.save(pointLog); // 포인트 로그 저장

                reservation.setPointsAwarded(false); // 포인트 회수 완료 표시
                log.info("사용자 ID '{}'에게 예약 ID '{}' 거절로 {} 포인트 회수. 현재 포인트: {}",
                        user.getUserId(), reservationId, refundedPoints, newPointBalance);

                log.info("예약 ID '{}' 거절 요청: 환불을 시도합니다.", reservationId);

                Optional<ReservationPayment> paymentOpt = reservationPaymentRepository.findByReservationAndStatus(reservation, "paid");

                if (paymentOpt.isEmpty()) {
                    log.warn("예약 ID '{}' 에 연결된 결제 정보가 없거나, 결제 상태가 PAID가 아니어서 환불을 진행할 수 없습니다.", reservationId);
                    throw new BasicException(ErrorCode.NOTFOUNT_MERCHANTUID);
                }

                ReservationPayment paymentToRefund = paymentOpt.get();
                String impUidToRefund = paymentToRefund.getImpUid();
                Integer amountToRefund = paymentToRefund.getOriginalAmount();

                if (impUidToRefund == null || impUidToRefund.isEmpty()) {
                    log.warn("예약 ID '{}'의 결제 정보(impUid)가 불완전하여 환불을 진행할 수 없습니다. impUid: {}", reservationId, impUidToRefund);
                    throw new BasicException(ErrorCode.NOTFOUNT_MERCHANTUID);
                }

                try {
                    // PaymentService를 통해 환불 진행 (impUid, BigDecimal amount, reason 전달)
                    // reason은 ownerNotes를 사용하거나 기본 문자열을 사용할 수 있습니다.
                    String refundReason = (ownerNotes != null && !ownerNotes.isEmpty()) ? ownerNotes : "예약 거절에 따른 자동 환불";
                    paymentService.cancelPayment(impUidToRefund, new BigDecimal(amountToRefund), refundReason); // ✅ 정확한 파라미터 전달

                    // 환불 성공 후 ReservationPayment의 상태를 CANCELLED로 업데이트
                    paymentToRefund.setStatus("예약 취소");
                    paymentToRefund.setPaidAt(LocalDateTime.now()); // ✅ setCancelledAt으로 수정
                    reservationPaymentRepository.save(paymentToRefund);
                    log.info("ReservationPayment ID '{}' 상태가 CANCELLED로 업데이트되었습니다.", paymentToRefund.getPaymentId());

                    log.info("예약 ID '{}' (impUid: {})에 대한 환불이 성공적으로 처리되었습니다.", reservationId, impUidToRefund);
                } catch (IamportResponseException | IOException e) {
                    // ✅ IamportResponseException, IOException 처리
                    log.error("[{}] 예약 ID '{}'에 대한 환불 실패 (PortOne 통신 오류): {}",
                            ErrorCode.PAYMENT_CANCEL_FAILED, reservationId, e.getMessage(), e);
                    throw new BasicException(ErrorCode.PAYMENT_CANCEL_FAILED);
                } catch (BasicException e) {
                    // ✅ BasicException은 이미 ErrorCode를 포함하고 있으므로 해당 정보를 활용하여 다시 던집니다.
                    log.error("[{}] 예약 ID '{}'에 대한 환불 실패 (비즈니스 로직 오류): {}",
                            e.getErrorCode(), reservationId, e.getMessage(), e);
                    throw e; // BasicException은 그대로 다시 던집니다.
                } catch (Exception e) { // ✅ 그 외 예상치 못한 일반 예외 처리
                    log.error("[{}] 예약 ID '{}'에 대한 환불 중 알 수 없는 오류 발생: {}",
                            ErrorCode.NOTFOUNT_MERCHANTUID, reservationId, e.getMessage(), e); // UNKNOWN_ERROR 코드 필요
                    throw new BasicException(ErrorCode.NOTFOUNT_MERCHANTUID);
                }
            }
        }

        reservationRepository.save(reservation);    // DB에 변경된 예약 정보 저장
        log.info("예약 ID '{}' 의 상태가 '{}' 에서 '{}'(으)로 변경되었습니다. 사장님 메모: '{}'",
                reservationId, reservation.getStatus(), newStatus, ownerNotes);


        if (user != null) {
            String title;
            String body;

            String restaurantName = reservation.getRestaurant().getRestaurantName();

            // 새로운 상태에 따라 알림 제목과 본문 설정
            if (STATUS_APPROVED.equals(reservation.getStatus())) {
                title = "예약 승인 알림입니다.";
                body = String.format("🎉 고객님!  %s 식당 예약이 승인되었습니다! %s %s에 만나요!",
                        restaurantName, reservation.getDate(), reservation.getTime());
            } else if (STATUS_REJECTED.equals(reservation.getStatus())) {
                title = "예약 거절 알림입니다.";
                body = String.format("😅 고객님.. %s 식당 예약이 거절되었습니다. 사유: %s",
                        restaurantName, (ownerNotes != null && !ownerNotes.isEmpty() ? ownerNotes : "자세한 내용은 식당에 문의해주세요."));
            } else {
                // 승인/거절 외의 상태 변화는 알림 보내지 않음 (필요시 추가)
                log.info("예약 ID '{}' 상태 '{}'는 알림 전송 대상이 아닙니다.", reservationId, newStatus);
                return; // 메서드 종료
            }

            fcmService.sendNotificationToUser(user, title, body);
            log.info("사용자 ID '{}'에게 알림 전송 시도: 제목='{}', 내용='{}'", user.getId(), title, body);

            Notification notification = Notification.builder()
                    .user(user) // 알림을 받은 사용자
                    .reservation(reservation) // 관련된 예약 정보
                    .title(title) // 알림 제목
                    .body(body) // 알림 본문
                    .isRead(false) // 초기 상태는 '읽지 않음'
                    .build();
            notificationRepository.save(notification);
            log.info("알림 내역이 DB에 저장되었습니다: 사용자 ID '{}', 예약 ID '{}'", user.getId(), reservationId);
        } else {
            log.warn("예약 ID '{}' 에 연결된 사용자 정보가 없어 알림을 보낼 수 없습니다.", reservationId);
        }
    } // <<-- updateReservationStatus 메서드의 닫는 중괄호


    /** 대기 중인 예약 목록 조회 */
    @Override
    @Transactional(readOnly = true)
    public List<PendingReservationDto> getPendingReservations(String ownerUserId) throws BasicException {
        Restaurant restaurant = restaurantRepository.findByOwnerUserId(ownerUserId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        List<Reservation> list = reservationRepository.findReservationsByRestaurant(
                                    restaurant.getRestaurantId(), STATUS_PENDING_APPROVAL);

        return list.stream()
                .map(r -> new PendingReservationDto(
                        r.getReservationId(),
                        r.getUser().getUserId(),
                        r.getUser().getName(),
                        Boolean.TRUE.equals(r.getUser().getNoShow()),
                        r.getNumPeople(),
                        r.getDate(),
                        r.getTime(),
                        r.getStatus()
                ))
                .collect(Collectors.toList());
    }

    /** 노쇼 대상 목록 조회 추가 */
    @Override
    @Transactional(readOnly = true)
    public List<NoShowReservationDto> getNoShowCandidates(String ownerUserId) throws BasicException {
        Restaurant restaurant = restaurantRepository.findByOwnerUserId(ownerUserId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        // "예약 완료" 상태이면서, 예약 방문일이 지난 건
        List<Reservation> list = reservationRepository.findReservationsByBeforeDate(
                                    STATUS_APPROVED, LocalDate.now().toString());

        return list.stream()
                .map(r -> new NoShowReservationDto(
                        r.getReservationId(),
                        r.getUser().getUserId(),
                        r.getUser().getName(),
                        r.getDate(),
                        r.getTime()
                ))
                .collect(Collectors.toList());
    }

    /** 노쇼 처리 추가 */
    @Override
    @Transactional
    public void markNoShow(Long reservationId) throws BasicException {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESERVATION_NOT_FOUND));

        // 예약 상태가 '예약 완료'인 경우에만 노쇼 처리 허용
        if (!STATUS_APPROVED.equals(r.getStatus())) {
            throw new BasicException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        r.setStatus("노쇼");  // 변경
        User u = r.getUser();
        u.setNoShow(true);     // 사용자 noShow 플래그 설정

        reservationRepository.save(r);    // 예약 저장
        userRepository.save(u);           // 사용자 저장
    }
}
