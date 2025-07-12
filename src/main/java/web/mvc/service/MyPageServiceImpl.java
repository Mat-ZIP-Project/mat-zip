package web.mvc.service;

import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.*;
import web.mvc.dto.*;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
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
    private final MeetingRepository meetingRepository;
    private final MeetupReviewRepository meetupReviewRepository;
    private final MeetupParticipantRepository meetupParticipantRepository;

    private final PaymentService paymentService;
    // 모임 rep 주입 필요
    private final ReservationPaymentRepository reservationPaymentRepository;
    private final PointRepository pointRepository;
    private final NotificationRepository notificationRepository;
    private final UserLikeRepository userLikeRepository;

    /**
     *  사용자 찜한 식당 내역 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<RestaurantLikeDetailDto> getUserRestaurantLikes(Long id) throws BasicException {
        User user = userRepository.findById(id).orElseThrow(() -> new BasicException(ErrorCode.USER_NOT_FOUND));

        List<UserLike> userLikes = userLikeRepository.findAllByUser(user);

        return userLikes.stream().map(userLike -> {
            //
            return RestaurantLikeDetailDto.builder()
                    .likeId(userLike.getLikeId())
                    .likedAt(userLike.getLikedAt())
                    .restaurantId(userLike.getRestaurant().getRestaurantId())
                    .restaurantName(userLike.getRestaurant().getRestaurantName())
                    .address(userLike.getRestaurant().getAddress())
                    .category(userLike.getRestaurant().getCategory())
                    .avgRating(userLike.getRestaurant().getAvgRating())
                    .phone(userLike.getRestaurant().getPhone())
                    .descript(userLike.getRestaurant().getDescript())
                    .openTime(userLike.getRestaurant().getOpenTime())
                    .closeTime(userLike.getRestaurant().getCloseTime())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     *  사용자 전체 예약 내역 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReservationDetailDto> getUserReservations(Long id) throws BasicException {

//        List<Reservation> reservations = reservationRepository.findByUserIdAndStatusIsNot(id, "대기중");

        List<Reservation> reservations = reservationRepository.findByUserIdAndStatusIn(id, Arrays.asList("예약 완료", "예약 거절"));
        // 조회된 예약 리스트를 ReservationDetailDto로 변환
        return reservations.stream()
                .map(reservation -> {
                    String paymentStatus;
                    Optional<ReservationPayment> paymentOpt = reservationPaymentRepository.findByReservation(reservation);

                    if (paymentOpt.isEmpty()) {
                        return null;
                    }
                    paymentStatus = paymentOpt.get().getStatus();
                    return ReservationDetailDto.builder()
                            .reservationId(reservation.getReservationId())
                            .reservationId(reservation.getReservationId())
                            .restaurantName(reservation.getRestaurant() != null ? reservation.getRestaurant().getRestaurantName() : "알 수 없음")
                            .date(LocalDate.parse(reservation.getDate()))
                            .time(LocalTime.parse(reservation.getTime()))
                            .numPeople(reservation.getNumPeople())
                            .status(reservation.getStatus())
                            .ownerNotes(reservation.getOwnerNotes())
                            .createdAt(reservation.getCreatedAt())
                            .paymentStatus(paymentStatus)
                            .build();
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     *  사용자 전체 리뷰 내역
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewDetailDto> getUserReviews(Long id) throws BasicException {
        try {
            List<Review> reviews = reviewRepository.findByUserId(id);

            // Review 리스트를 ReviewDetailDto 리스트로 변환
            return reviews.stream()
                    .map(review -> ReviewDetailDto.builder()
                            .reviewId(review.getReviewId())
                            .content(review.getContent())
                            .rating(review.getRating())
                            .reviewedAt(review.getReviewedAt())
                            .visitDate(review.getVisitDate())
                            .restaurantName(review.getRestaurant() != null ? review.getRestaurant().getRestaurantName() : "알 수 없음")
                            .build())
                    .collect(Collectors.toList());
        } catch (BasicException e) {
            throw new BasicException(ErrorCode.RESERVATION_NOT_FOUND);
        }
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
        if (!(currentStatus.equals("예약 완료")
        || currentStatus.equals("결제 후 사장 승인 대기"))) {
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
            if (payment.getStatus().equals("결제 완료")) {
                try {
                    paymentService.cancelPayment(
                            payment.getImpUid(),
                            new BigDecimal(payment.getOriginalAmount()), // ReservationPayment의 amount를 사용
                            "사용자 요청에 의한 예약 취소 환불"
                    );

                    // 환불 성공 시 결제 상태 업데이트 (PaymentService에서도 하지만 여기서 한 번 더 명시적으로)
                    payment.setStatus("결제 취소");
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
        reservation.setStatus("예약 취소");
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
     *  사용자의 포인트 잔액을 조회
     */
    @Override
    public Integer getUserPointBalance(Long id) throws BasicException {
        try {
            Optional<User> userPoint = userRepository.findById(id);

            if (userPoint.isPresent()) {
                User user = userPoint.get();
                return user.getPointBalance();
            } else {
                throw new BasicException(ErrorCode.USER_NOT_FOUND);
            }
        } catch (BasicException e) {
            throw new BasicException(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Override
    public List<Point> getUserPointHistory(Long id) throws BasicException {
        try {
            List<Point> pointHistory;
            pointHistory = pointRepository.findByUserId(id);
            return pointHistory;
        } catch (BasicException e) {
            throw new BasicException(ErrorCode.USER_NOT_FOUND);
        }
    }

    /**
     *  사용자의 모든 알림을 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDetailDto> getUserNotification(Long id) throws BasicException {
        try {
            List<Notification> notifications = notificationRepository.findByUserId(id);
            // 조회된 Notification 엔티티 리스트를 NotificationDetailDto 리스트로 변환합니다.
            return notifications.stream()
                    .map(notification -> {
                        Long reservationId = null;
                        String restaurantName = "알 수 없음"; // 기본값 설정

                        // Notification 엔티티의 reservation 필드에 접근합니다. (EAGER 로딩)
                        Reservation reservation = notification.getReservation();
                        if (reservation != null) {
                            reservationId = reservation.getReservationId();

                            // Reservation 엔티티의 restaurant 필드에 접근합니다. (일반적으로 EAGER 로딩)
                            Restaurant restaurant = reservation.getRestaurant();
                            if (restaurant != null) {
                                restaurantName = restaurant.getRestaurantName();
                            }
                        }

                        // NotificationDetailDto를 빌더 패턴으로 생성하여 반환합니다.
                        return NotificationDetailDto.builder()
                                .notificationId(notification.getNotificationId())
                                .title(notification.getTitle())
                                .body(notification.getBody())
                                .isRead(notification.getIsRead())
                                .createdAt(notification.getCreatedAt())
                                .reservationId(reservationId)
                                .restaurantName(restaurantName)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (BasicException e) {
            throw new BasicException(ErrorCode.NO_AUTH_LOGS);
        }
    }

    /**
     *  사용자의 모든 알림을 읽음 상태로 변경
     */
    @Override
    public void markNotificationAsRead(Long id) throws BasicException {
        try {
            List<Notification> unreadNotification = notificationRepository.findByUserIdAndIsRead(id, false);

            if (unreadNotification.isEmpty()) {
                return;
            }

            for (Notification notification : unreadNotification) {
                notification.setIsRead(true);
            }
            notificationRepository.saveAll(unreadNotification);
        } catch (BasicException e) {
            throw new BasicException(ErrorCode.NO_AUTH_LOGS);
        }
    }

    @Override
    public int getUnreadNotificationCount(Long id) throws BasicException {
        try {
            return notificationRepository.countByUserIdAndIsRead(id, false);
        } catch (BasicException e) {
            throw new BasicException(ErrorCode.NO_AUTH_LOGS);
        }
    }

    @Override
    public User updateUserPreference(Long id, UserPreferenceDto userPreferenceDto) throws BasicException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        user.setPreferenceCategory(userPreferenceDto.getPreferenceCategory());
        return userRepository.save(user);
    }

    /**
     *  마이페이지에서 리뷰 삭제 로직
     */
    @Override
    public void deleteReview(Long id, Long reviewId) throws BasicException {

        Review reviewToDelete = reviewRepository.findByReviewIdAndUserId(reviewId, id)
                .orElseThrow(() -> new BasicException(ErrorCode.USER_NOT_FOUND));

        // 2. 리뷰 삭제
        reviewRepository.delete(reviewToDelete);
    }
}
