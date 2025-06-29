package web.mvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.Reservation;
import web.mvc.domain.ReservationPayment;
import web.mvc.domain.Review;
import web.mvc.dto.ReservationDetailDto;
import web.mvc.exception.BasicException;
import web.mvc.repository.ReservationPaymentRepository;
import web.mvc.repository.ReservationRepository;
import web.mvc.repository.ReviewRepository;
import web.mvc.repository.UserRepository;
import web.mvc.util.Enums;

import java.time.LocalDateTime;
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
    // 모임 rep 주입 필요
    private final ReservationPaymentRepository reservationPaymentRepository;

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

    @Override
    @Transactional(readOnly = true)
    public List<Review> getUserReviews(Long id) throws BasicException {

        List<Review> reviews = reviewRepository.findByUserId(id);
        return reviews;
    }
}
