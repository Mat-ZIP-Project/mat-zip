package web.mvc.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import web.mvc.domain.Reservation;
import web.mvc.domain.ReservationPayment;
import web.mvc.domain.Restaurant;
import web.mvc.domain.User;
import web.mvc.dto.OwnerActionRequestDto;
import web.mvc.dto.PortOneWebhookDto;
import web.mvc.dto.ReservationRequestDto;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.ReservationPaymentRepository;
import web.mvc.repository.ReservationRepository;
import web.mvc.repository.RestaurantRepository;
import web.mvc.repository.UserRepository;
import web.mvc.util.Enums;

import java.util.UUID;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationPaymentRepository reservationPaymentRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final NotificationService notificationService;

    // @Autowired 어노테이션을 생성자에 명시적으로 추가
    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  ReservationPaymentRepository reservationPaymentRepository,
                                  UserRepository userRepository,
                                  RestaurantRepository restaurantRepository,
                                  NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.reservationPaymentRepository = reservationPaymentRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.notificationService = notificationService;
    }

    // 사용자 Id로 사용자 조회
    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BasicException(ErrorCode.NOTFOUND_ID));
    }
    // 식당 Id로 조회
    private Restaurant getRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new BasicException(ErrorCode.NOTFOUND_ID));
    }
    // 예약 Id로 조회
    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BasicException(ErrorCode.NOTFOUND_ID));
    }

    // 예약 관련 사용자에게 알림을 전송
    private void sendReservationNotification(User user, Reservation reservation, String title, String body) {
        notificationService.sendNotification(user, title, body, reservation.getReservationId().toString());
    }

    @Override
    @Transactional
    public Reservation initiateReservation(ReservationRequestDto requestDto) {
        User user = getUser(requestDto.getId());
        Restaurant restaurant = getRestaurant(requestDto.getId());

        int calAmount = requestDto.getNumPeople() * 10000;
        System.out.println("예약 인원: "+ requestDto.getNumPeople() + "예약금 얼마? "+calAmount);

        // 새로운 reservation 객체 생성
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setRestaurant(restaurant);
        reservation.setNumPeople(requestDto.getNumPeople());
        reservation.setDate(requestDto.getDate());
        reservation.setTime(requestDto.getTime());
        reservation.setStatus(String.valueOf(Enums.ReservationStatus.PENDING));

        // 생성된 예약 엔티티를 데이터베이스에 저장
        reservation = reservationRepository.save(reservation);
        // PortOne 결제에 사용할 고유한 가맹점 주문번호를 생성
        String merchantUid = "res-" + UUID.randomUUID().toString();

        // 새로운 예약금 정보를 생성
        ReservationPayment tempPayment = new ReservationPayment();
        tempPayment.setReservation(reservation);
        tempPayment.setUser(user);
        tempPayment.setAmount(calAmount);
        tempPayment.setStatus(Enums.PaymentStatus.READY);
        tempPayment.setMerchantUid(merchantUid);
        // 데이터베이스에 저장
        reservationPaymentRepository.save(tempPayment);

        System.out.println("예약 초기화 및 결제 대기: Reservation ID: " + reservation.getReservationId() + ", Merchant UID: " + merchantUid);

        return reservation;
    }

    // PortOne으로부터 받은 웹훅 데이터 받아서
    @Override
    public boolean handlePortOneWebhook(PortOneWebhookDto webhookDto) {
        return false;
    }

    @Override
    public Reservation handleOwnerAction(Long reservationId, OwnerActionRequestDto actionDto) {
        return null;
    }

    @Override
    public Reservation getReservationById(Long reservationId) {
        return null;
    }
}
