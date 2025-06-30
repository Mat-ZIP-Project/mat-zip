package web.mvc.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.FcmToken;
import web.mvc.domain.Reservation;
import web.mvc.domain.User;
import web.mvc.exception.BasicException;
import web.mvc.repository.FcmTokenRepository;
import web.mvc.repository.ReservationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationRemindedService {

    private final ReservationRepository reservationRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmService fcmService;

    // String 날짜/시간 파싱을 위한 DateTimeFormatter 정의
    // 예약 날짜 형식: "YYYY-MM-DD"
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // 예약 시간 형식: "HH:MM" (초 단위가 없는 경우)
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


    /**
     * 매 분(Minute)마다 예약 알림 대상을 확인하고 알림을 전송합니다.
     * Cron 표현식 "0 * * * * ?"는 "매 분 0초"를 의미합니다.
     * 스케줄러는 백그라운드에서 동작하며, 프론트엔드와 직접적인 통신은 없습니다.
     * 알림 전송은 FCM 서비스를 통해 비동기적으로 이루어집니다.
     */
    @Scheduled(cron = "0 * * * * ?") // 매 분 0초마다 실행
    @Transactional // DB 조회 및 업데이트가 함께 이루어지므로 트랜잭션 필요
    public void sendReservationReminders() {
        log.info("예약 알림 스케줄러 실행: 현재 시간 {}", LocalTime.now());

        LocalDate today = LocalDate.now();
        String todayString = today.format(DATE_FORMATTER); // 오늘 날짜를 String으로 변환

        // 'APPROVED' 상태이고, 아직 알림을 보내지 않았으며, 현재 날짜에 해당하는 모든 예약을 조회합니다.
        List<Reservation> potentialReminders = reservationRepository.findReservationsForReminder(todayString);

        if (potentialReminders.isEmpty()) {
            log.info("오늘 {} 에 해당하는 예약 알림 대상이 없습니다.", todayString);
            return;
        }

        // 현재 시간을 기준으로 1시간 (59분 ~ 61분) 남은 예약을 필터링합니다.
        LocalDateTime currentDateTime = LocalDateTime.now();
        List<Reservation> upcomingReservations = potentialReminders.stream()
                .filter(reservation -> {
                    try {
                        // String 타입의 날짜와 시간을 파싱합니다.
                        LocalDate reservationDate = LocalDate.parse(reservation.getDate(), DATE_FORMATTER);
                        LocalTime reservationTime = LocalTime.parse(reservation.getTime(), TIME_FORMATTER);
                        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime);

                        // 현재 시간과 예약 시간의 분 차이를 계산합니다.
                        long minutesUntilReservation = ChronoUnit.MINUTES.between(currentDateTime, reservationDateTime);

                        // 예약 시간이 현재로부터 59분 초과 61분 이하 (즉, 약 1시간 남은 경우)
                        // 예를 들어, 현재 10:00:30이라면, 11:00:30 ~ 11:01:29 사이의 예약을 찾음
                        return minutesUntilReservation > 59 && minutesUntilReservation <= 61;

                    } catch (DateTimeParseException e) {
                        log.error("예약 ID {}의 날짜 또는 시간 형식 파싱 실패: 날짜='{}', 시간='{}', 오류={}",
                                reservation.getReservationId(), reservation.getDate(), reservation.getTime(), e.getMessage());
                        return false; // 파싱 실패한 예약은 스킵
                    }
                })
                .toList();

        if (upcomingReservations.isEmpty()) {
            log.info("필터링 결과, 현재 알림을 보낼 예약이 없습니다. (총 {}개 중)", potentialReminders.size());
            return;
        }

        log.info("총 {}개의 예약에 대해 알림을 보낼 예정입니다.", upcomingReservations.size());

        for (Reservation reservation : upcomingReservations) {
            User user = reservation.getUser();

            if (user == null) {
                continue;
            }

            String title = "🔔 예약 알림입니다!";
            String body = String.format("🎉 고객님 %s 식당 예약이 1시간 뒤인 %s 입니다! 잊지 마세요!",
                    reservation.getRestaurant().getRestaurantName(),
                    reservation.getTime()); // String 그대로 사용

            try {
                // FcmService를 통해 알림 전송
                fcmService.sendNotificationToUser(user, title, body);
                log.info("예약 알림 성공: 예약 ID={}, 사용자 ID={}, 시간={}",
                        reservation.getReservationId(), user.getUserId(), reservation.getTime());

                // 알림 전송 후 reminded 플래그를 true로 변경하여 중복 전송 방지
                reservation.setReminded(true);
                reservationRepository.save(reservation);
                log.debug("예약 ID {}의 reminded 플래그가 true로 업데이트되었습니다.", reservation.getReservationId());

            } catch (BasicException e) {
                log.error("예약 ID {}에 대한 알림 전송 중 비즈니스 로직 오류: {}", reservation.getReservationId(), e.getMessage(), e);
            } catch (Exception e) {
                log.error("예약 ID {}에 대한 알림 전송 중 알 수 없는 오류: {}", reservation.getReservationId(), e.getMessage(), e);
            }
        }
        log.info("예약 알림 스케줄러 종료.");
    }




}
