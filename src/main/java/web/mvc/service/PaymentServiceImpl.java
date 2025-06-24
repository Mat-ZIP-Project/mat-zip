package web.mvc.service;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.request.PrepareData;
import com.siot.IamportRestClient.response.IamportResponse;
//import com.siot.IamportRestClient.response.Payment;
import com.siot.IamportRestClient.response.Prepare;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import web.mvc.domain.Payment;
import web.mvc.domain.Reservation;
import web.mvc.domain.User;
import web.mvc.dto.PaymentCompleteReqDto;
import web.mvc.dto.PaymentCompleteResDto;
import web.mvc.dto.PreparationReqDto;
import web.mvc.dto.PreparationResDto;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.PaymentRepository;
import web.mvc.repository.ReservationRepository;
import web.mvc.repository.UserRepository;
import web.mvc.util.Enums;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {


    private final IamportClient client;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PreparationResDto prepareValid(PreparationReqDto request) throws BasicException, IamportResponseException, IOException {
        Long reservationId = request.getReservationId();
        Integer amount = request.getAmount();

        log.info("사전 검증 요청 ",reservationId, amount);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESERVATION_NOT_FOUND));

        User user = reservation.getUser();
        if(user==null) {
            throw new BasicException(ErrorCode.USER_INFO_MISSING);
        }

        // 중복 사전 검증 방지
        Optional<Payment> existingPayment = paymentRepository.findByReservationAndStatus(reservation, Enums.PaymentStatus.READY);
        if (existingPayment.isPresent()) {
            throw new BasicException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        String merchantUid = generateMerchantUid(reservationId);

        Payment newPayment = Payment.builder()
                .reservation(reservation)
                .amount(amount)
                .status(Enums.PaymentStatus.READY)
                .user(user)
                .merchantUid(merchantUid)
                .build();

        try {
            paymentRepository.save(newPayment);
        } catch (Exception e) {
            throw new BasicException(ErrorCode.PAYMENT_DB_ERROR);
        }

        PrepareData prepareData = new PrepareData(merchantUid, new BigDecimal(amount));
        IamportResponse<Prepare> iamportResponse = client.postPrepare(prepareData);

        if (iamportResponse.getCode() == 0) {
            PreparationResDto response = new PreparationResDto();
            response.setSuccess(true);
            response.setReservationId(reservationId);
            response.setMerchantUid(merchantUid);
            response.setAmount(amount);
            response.setMessage("PortOne 사전 검증 성공. 결제창을 띄울 수 있습니다.");
            log.info("PortOne 사전 검증 성공: merchantUid={}", merchantUid);
            return response;
        } else {
            log.error("PortOne 사전 검증 실패: {} - {}", iamportResponse.getCode(), iamportResponse.getMessage());
            newPayment.setStatus(Enums.PaymentStatus.FAILED);
            paymentRepository.save(newPayment);
            throw new BasicException(ErrorCode.PAYMENT_PREPARE_FAILED);
        }
    }

    @Override
    @Transactional
    public PaymentCompleteResDto completePayment(PaymentCompleteReqDto request) throws BasicException {
        String impUid = request.getImpUid();
        String merchantUid = request.getMerchantUid();

        log.info("결제 완료 요청 - impUid: {}, merchantUid: {}", impUid, merchantUid);

        com.siot.IamportRestClient.response.Payment portonePaymentData;
        try {
            IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = client.paymentByImpUid(impUid);
            portonePaymentData = iamportResponse.getResponse();

            if (iamportResponse.getCode() != 0 || portonePaymentData == null) {
                log.error("PortOne 결제 정보 조회 실패: {} - {}", iamportResponse.getCode(), iamportResponse.getMessage());
                throw new BasicException(ErrorCode.PAYMENT_INFO_NOT_FOUND);
            }
        } catch (IamportResponseException | IOException e) {
            log.error("PortOne 연동 오류 또는 네트워크 오류 (impUid: {}): {}", impUid, e.getMessage(), e);
            throw new BasicException(ErrorCode.PAYMENT_INFO_NOT_FOUND);
        }

        Payment payment = paymentRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> {
                    log.error("DB에 merchant_uid {} 에 해당하는 Payment 정보가 없음.", merchantUid);
                    // 이 경우, PortOne 결제는 성공했지만 우리 DB에 해당 merchant_uid가 없다는 의미이므로,
                    // 즉시 해당 결제를 취소(환불)하는 로직을 추가하는 것을 고려해야 합니다.
                    // 예외를 던지기 전에 cancelPayment를 호출하는 것도 좋은 방법입니다.
                    // cancelPayment(impUid, new BigDecimal(portonePaymentData.getAmount()), "DB에 merchant_uid 불일치");
                    return new BasicException(ErrorCode.NOTFOUNT_MERCHANTUID);
                });

        // 2. 이미 처리된 결제인지 확인 (중복 콜백 방지)
        if (payment.getStatus() == Enums.PaymentStatus.PAID) {
            log.warn("이미 처리된 결제입니다: impUid={}, merchantUid={}", impUid, merchantUid);
            throw new BasicException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        // 3. 결제 위변조 최종 검증 및 Payment 엔티티 업데이트
        // 이 메서드 내에서 예외가 발생하면 completePayment도 예외를 던집니다.
        validateAndProcessPayment(payment, portonePaymentData);

        // 4. 모든 검증 성공: Reservation 정보 업데이트
        Reservation reservation = payment.getReservation();
        if (reservation != null) {
            reservation.setStatus("예약완료"); // Reservation 엔티티의 status 타입에 맞게
            reservationRepository.save(reservation);
            log.info("Reservation 엔티티 상태 업데이트 (예약완료): reservationId={}", reservation.getReservationId());
        }

        // 응답 DTO를 PaymentCompleteResDto로 반환
        PaymentCompleteResDto response = new PaymentCompleteResDto();
        response.setSuccess(true);
        response.setMerchantUid(merchantUid);
        response.setImpUid(impUid);
        response.setStatus("PAID"); // 이 상태는 Enums.PaymentStatus.PAID와 일치해야 합니다.
        response.setMessage("결제가 성공적으로 처리되었습니다.");
        log.info("결제 및 예약 정보 DB 저장 완료: impUid={}, merchantUid={}", impUid, merchantUid);
        return response;
    }

//    @Override
//    @Transactional
//    public boolean cancelPayment(String impUid, BigDecimal amount, String reason) throws BasicException {
//        try {
//            log.warn("PortOne 결제 취소 요청 - impUid: {}, amount: {}, reason: {}", impUid, amount, reason);
//
//            // CancelData 객체 생성
//            CancelData cancelData = new CancelData(impUid, true); // imp_uid를 사용하여 취소, checksum=true
//            // 필요에 따라 amount, reason 등을 설정
//            if (amount != null) {
//                cancelData.setAmount(amount); // BigDecimal 타입으로 설정 가능 (SDK에 따라 doubleValue() 필요할 수도 있음)
//            }
//            if (reason != null && !reason.isEmpty()) {
//                cancelData.setReason(reason);
//            }
//            // cancel_request_amount도 필요하면 설정: cancelData.setCancel_request_amount(amount);
//
//            // CancelData 객체를 사용하여 취소 API 호출
//            IamportResponse<Payment> cancelResponse = client.cancelPaymentByImpUid(cancelData);
//
//            if (cancelResponse.getCode() == 0) {
//                log.info("PortOne 결제 취소 성공: impUid={}", impUid);
//                paymentRepository.findByImpUid(impUid).ifPresent(payment -> {
//                    payment.setStatus(Enums.PaymentStatus.CANCELLED);
//                    payment.setPaidAt(LocalDateTime.now());
//                    paymentRepository.save(payment);
//
//                    if (payment.getReservation() != null) {
//                        Reservation reservation = payment.getReservation();
//                        reservation.setStatus("예약취소");
//                        reservationRepository.save(reservation);
//                    }
//                });
//                return true;
//            } else {
//                log.error("PortOne 결제 취소 실패: {} - {}", cancelResponse.getCode(), cancelResponse.getMessage());
//                throw new BasicException(ErrorCode.PAYMENT_CANCEL_FAILED);
//            }
//        } catch (IamportResponseException | IOException e) {
//            log.error("PortOne 결제 취소 중 오류 발생: {}", e.getMessage(), e);
//            throw new BasicException(ErrorCode.PAYMENT_CANCEL_FAILED);
//        }
//    }


    // private 메서드는 인터페이스에 정의하지 않고 구현체 내부에 유지합니다.
    private String generateMerchantUid(Long reservationId) {
        return "res_" + reservationId + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void validateAndProcessPayment(
            Payment payment,
            com.siot.IamportRestClient.response.Payment portonePaymentData) {

        if (!portonePaymentData.getStatus().equals("paid")) {
            log.warn("결제 상태 불일치 (paid 아님): impUid={}, status={}", portonePaymentData.getImpUid(), portonePaymentData.getStatus());
            payment.setStatus(Enums.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new BasicException(ErrorCode.PAYMENT_NOT_PAID);
        }

        if (!portonePaymentData.getMerchantUid().equals(payment.getMerchantUid())) {
            log.error("Merchant UID 불일치: PortOne={}, DB={}", portonePaymentData.getMerchantUid(), payment.getMerchantUid());
//            cancelPayment(portonePaymentData.getImpUid(), portonePaymentData.getAmount(), "주문번호 불일치로 인한 자동 환불");
            payment.setStatus(Enums.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new BasicException(ErrorCode.PAYMENT_MERCHANT_UID_MISMATCH);
        }

        if (portonePaymentData.getAmount().intValue() != payment.getAmount().intValue()) {
            log.error("결제 금액 불일치: PortOne={}, Expected={}", portonePaymentData.getAmount(), payment.getAmount());
//            cancelPayment(portonePaymentData.getImpUid(), portonePaymentData.getAmount(), "결제 금액 불일치로 인한 자동 환불");
            payment.setStatus(Enums.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new BasicException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        payment.setImpUid(portonePaymentData.getImpUid());
        payment.setPaidAt(LocalDateTime.now());
        payment.setStatus(Enums.PaymentStatus.PAID);
        paymentRepository.save(payment);

        log.info("결제 유효성 검증 성공 및 Payment 엔티티 업데이트 완료: paymentId={}, impUid={}", payment.getPaymentId(), portonePaymentData.getImpUid());
    }
}
