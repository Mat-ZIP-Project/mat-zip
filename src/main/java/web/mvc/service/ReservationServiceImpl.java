package web.mvc.service;

//import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.Notification;
import web.mvc.domain.Reservation;
import web.mvc.domain.Restaurant;
import web.mvc.domain.User;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.NotificationRepository;
import web.mvc.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final FcmService fcmService;
    private final NotificationRepository notificationRepository;


    /**
     *  특정 예약 상태를 승인( APPROVED ) 또는 거절( REJECTED ) 등으로 업데이트
     *  사장님 메모( ownerNotes )와 처리 시각( approvedAt )을 기록
     *  FCM 알림 전송 : FcmService의 메서드를 호출하여 해당 예약 사용자에게 알림을 보낸다.
     *  NotificationRepository를 통해 DB에 알림 발송 내역을 저장.
     */
    @Override
    @Transactional
    public void updateReservationStatus(Long reservationId, String newStatus, String ownerNotes) throws BasicException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESERVATION_NOT_FOUND));

        List<String> validStatuses = List.of("PENDING", "PENDING_APPROVAL", "APPROVED", "REJECTED", "CANCELLED");
        String statusToUpdate = newStatus.toUpperCase();
        String currentStatus = reservation.getStatus();

        if (!validStatuses.contains(statusToUpdate)) {
            throw new BasicException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        // 2. 예약 상태 전환 유효성 검사 (비즈니스 규칙 강화)
        // PENDING 또는 PENDING_APPROVAL 상태에서만 APPROVED 또는 REJECTED로 전환 가능
        if (("APPROVED".equals(statusToUpdate) || "REJECTED".equals(statusToUpdate))) {
            if (!("PENDING".equals(currentStatus) || "PENDING_APPROVAL".equals(currentStatus))) {
                log.error("예약 상태 전환 불가: 예약 ID '{}', 현재 상태 '{}' 에서 '{}'로 전환할 수 없음. (승인/거절은 PENDING/PAID_PENDING_APPROVAL 에서만 가능)",
                        reservationId, currentStatus, statusToUpdate);
                throw new BasicException(ErrorCode.INVALID_RESERVATION_STATUS_TRANSITION);
            }
        }
        // 이미 APPROVED/REJECTED/CANCELLED 상태인 예약을 PENDING/PENDING_APPROVAL로 되돌리려는 시도 방지
        if (("PENDING".equals(statusToUpdate) || "PENDING_APPROVAL".equals(statusToUpdate))) {
            if (("APPROVED".equals(currentStatus) || "REJECTED".equals(currentStatus) || "CANCELLED".equals(currentStatus))) {
                log.error("예약 상태 전환 불가: 예약 ID '{}', 이미 최종 상태인 '{}' 에서 '{}'로 되돌릴 수 없음.",
                        reservationId, currentStatus, statusToUpdate);
                throw new BasicException(ErrorCode.INVALID_RESERVATION_STATUS_TRANSITION);
            }
        }

        reservation.setStatus(statusToUpdate);
        reservation.setOwnerNotes(ownerNotes);

        if ("APPROVED".equals(statusToUpdate) || "REJECTED".equals(statusToUpdate)) {
            reservation.setApprovedAt(LocalDateTime.now());
        }

        reservationRepository.save(reservation);    // DB에 변경된 예약 정보 저장
        log.info("예약 ID '{}' 의 상태가 '{}' 에서 '{}'(으)로 변경되었습니다. 사장님 메모: '{}'",
                reservationId, reservation.getStatus(), statusToUpdate, ownerNotes);

        User user = reservation.getUser();
        if (user != null) {
            String title;
            String body;

            String restaurantName = "맛있는 식당";
            Restaurant restaurant = reservation.getRestaurant();
            if (restaurant != null && restaurant.getRestaurantName() != null) {
                restaurantName = restaurant.getRestaurantName();
            }

            // 새로운 상태에 따라 알림 제목과 본문 설정
            if ("APPROVED".equals(statusToUpdate)) {
                title ="예약 완료 알림입니다.";
                body = restaurantName + "식당 예약이 완료되었습니다.";
            } else if ("REJECTED".equals(statusToUpdate)) {
                title = "예약 거절 알림입니다.";
                body = restaurantName + "예약이 거절되었습니다." + (ownerNotes != null && !ownerNotes.isEmpty() ? ownerNotes : "영업 종료 시간입니다.");
            } else {
                return;
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

    }
}
