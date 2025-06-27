//package web.mvc.service;
//
//import com.siot.IamportRestClient.IamportClient;
//import com.siot.IamportRestClient.exception.IamportResponseException;
//import com.siot.IamportRestClient.request.PrepareData;
//import com.siot.IamportRestClient.response.IamportResponse;
//import com.siot.IamportRestClient.response.Payment;
//import com.siot.IamportRestClient.response.Prepare;
//import org.springframework.transaction.annotation.Transactional; // <-- 반드시 이 스프링 Transactional을 import 해야 합니다!
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import web.mvc.domain.Reservation;
//import web.mvc.domain.ReservationPayment;
//import web.mvc.dto.PaymentCompleteReqDto;
//import web.mvc.dto.PaymentCompleteResDto;
//import web.mvc.dto.PreparationReqDto;
//import web.mvc.dto.PreparationResDto;
//import web.mvc.exception.BasicException;
//import web.mvc.exception.ErrorCode;
//import web.mvc.repository.ReservationPaymentRepository;
//import web.mvc.repository.ReservationRepository;
//import web.mvc.repository.UserRepository;
//import web.mvc.util.Enums;
//
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentServiceImpl implements PaymentService {
//
//    private final IamportClient client;
//    private final ReservationRepository reservationRepository;
//    private final ReservationPaymentRepository reservationPaymentRepository;
//    private final UserRepository userRepository;
//
//    /**
//     *  예약 정보를 확인하고 merchant_uid( 주문번호 )를 생성
//     *  DB에 결제 정보를 READY 상태로 저장
//     *  아임포트 API의 사전 검증( postPrepare )을 호출하여 금액 위변조를 1차 방지한다.
//     */
//    @Override
//    @Transactional(rollbackFor = {IamportResponseException.class, IOException.class, BasicException.class}) // <-- 여기에 rollbackFor 속성 추가!
//    public PreparationResDto prepareValid(PreparationReqDto request) throws BasicException, IamportResponseException, IOException {
//        Long reservationId = request.getReservationId();
//        Integer amount = request.getAmount();
//
//        log.info("사전 검증 요청 사전 검증 prepareValid - reservationId: {}, amount: {}", reservationId, amount); // 로그 형식 개선
//
//        Reservation reservation = reservationRepository.findById(reservationId)
//                .orElseThrow(() -> new BasicException(ErrorCode.RESERVATION_NOT_FOUND));
//
//        User user = reservation.getUser();
//        if(user == null) { // 객체 비교는 == null 사용
//            throw new BasicException(ErrorCode.USER_INFO_MISSING);
//        }
//
//        // 중복 사전 검증 방지
//        Optional<ReservationPayment> existingPayment = reservationPaymentRepository.findByReservationAndStatus(reservation, Enums.PaymentStatus.READY);
//        if (existingPayment.isPresent()) {
//            throw new BasicException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
//        }
//
//        String merchantUid = generateMerchantUid(reservationId);
//
//        ReservationPayment newPayment = ReservationPayment.builder()
//                .reservation(reservation)
//                .amount(amount)
//                .status(Enums.PaymentStatus.READY)
//                .user(user)
//                .merchantUid(merchantUid) // Payment 엔티티의 DB 컬럼명이 portone_merchant_uid라면 이렇게 사용하는 것이 일관적입니다. Payment 엔티티의 필드명을 확인하세요.
//                .build();
//
//        try {
//            reservationPaymentRepository.save(newPayment); // 이 시점부터 트랜잭션이 시작됩니다.
//        } catch (Exception e) {
//            log.error("DB에 결제 정보 저장 실패: {}", e.getMessage(), e);
//            throw new BasicException(ErrorCode.PAYMENT_DB_ERROR);
//        }
//
//        BigDecimal amountToSendToPortOne = new BigDecimal(amount).setScale(0, RoundingMode.UNNECESSARY);
//        log.info("[PaymentServiceImpl] PortOne에 보낼 최종 금액 (BigDecimal): {}, merchantUid: {}", amountToSendToPortOne, merchantUid);
//
//        PrepareData prepareData = new PrepareData(merchantUid, amountToSendToPortOne);
//        IamportResponse<Prepare> iamportResponse; // try-catch 외부에서 선언하여 이후에도 접근 가능하게 함
//
//        try {
//            iamportResponse = client.postPrepare(prepareData); // 아임포트 사전 검증 API 호출
//        } catch (IamportResponseException e) {
//            log.error("PortOne 사전 검증 API 호출 중 IamportResponseException 발생: {}", e.getMessage(), e);
//            throw e; // 롤백을 트리거하기 위해 예외를 다시 던짐
//        } catch (IOException e) {
//            log.error("PortOne 사전 검증 API 호출 중 IOException 발생: {}", e.getMessage(), e);
//            throw e; // 롤백을 트리거하기 위해 예외를 다시 던짐
//        }
//
//        if (iamportResponse.getCode() == 0) {
//            PreparationResDto response = new PreparationResDto();
//            response.setSuccess(true);
//            response.setReservationId(reservationId);
//            response.setMerchantUid(merchantUid);
//            response.setAmount(amount);
//            response.setMessage("PortOne 사전 검증 성공. 결제창을 띄울 수 있습니다.");
//            log.info("PortOne 사전 검증 최종 성공: merchantUid={}", merchantUid);
//            return response;
//        } else {
//            // 아임포트 API 호출 자체는 성공했으나, 응답 코드가 0이 아닌 경우
//            log.error("PortOne 사전 검증 실패 (응답 코드 != 0): Code={}, Message={}", iamportResponse.getCode(), iamportResponse.getMessage());
//            // 이 시점에서 newPayment의 상태를 FAILED로 변경하고 저장할 필요는 없습니다.
//            // 아래에서 BasicException을 던지면 @Transactional에 의해 전체 트랜잭션이 롤백될 것입니다.
//            throw new BasicException(ErrorCode.PAYMENT_PREPARE_FAILED);
//        }
//    }
//
//    /**
//     * 클라이언트로부터 받은 impUid와 merchantUid를 사용
//     * 아임포트 API를 호출하여 실제 결제 정보를 조회
//     * DB에 저장된 정보와 아임포트 조회 정보를 비교하여 최종 위변조 검증을 수행
//     * 검증 성공 시 DB의 Reservation_payment 상태를 PAID로 업데이트하고,
//     * Reservation 상태도 "예약 완료"로 변경한다.
//     */
//    @Override
//    @Transactional // completePayment에도 @Transactional을 적용하여 DB 일관성 유지
//    public PaymentCompleteResDto completePayment(PaymentCompleteReqDto request) throws BasicException {
//        String impUid = request.getImpUid();
//        String merchantUid = request.getMerchantUid();
//
//        log.info("결제 완료 요청 - impUid: {}, merchantUid: {}", impUid, merchantUid);
//
//        // 아임포트 Payment 객체
//        Payment portonePaymentData;
//        try {
//            IamportResponse<Payment> iamportResponse = client.paymentByImpUid(impUid);
//            portonePaymentData = iamportResponse.getResponse();
//
//            if (iamportResponse.getCode() != 0 || portonePaymentData == null) {
//                log.error("PortOne 결제 정보 조회 실패: {} - {}", iamportResponse.getCode(), iamportResponse.getMessage());
//                throw new BasicException(ErrorCode.PAYMENT_INFO_NOT_FOUND);
//            }
//        } catch (IamportResponseException | IOException e) {
//            log.error("PortOne 연동 오류 또는 네트워크 오류 (impUid: {}): {}", impUid, e.getMessage(), e);
//            throw new BasicException(ErrorCode.PAYMENT_INFO_NOT_FOUND);
//        }
//
//        // DB 컬럼명이 portone_merchant_uid라면 repository 메서드 이름도 findByPortoneMerchantUid가 적합합니다.
//        ReservationPayment payment = reservationPaymentRepository.findByMerchantUid(merchantUid)
//                .orElseThrow(() -> {
//                    log.error("DB에 merchant_uid {} 에 해당하는 Payment 정보가 없음.", merchantUid);
//                    return new BasicException(ErrorCode.NOTFOUNT_MERCHANTUID);
//                });
//
//        // 2. 이미 처리된 결제인지 확인 (중복 콜백 방지)
//        if (payment.getStatus() == Enums.PaymentStatus.PAID) {
//            log.warn("이미 처리된 결제입니다: impUid={}, merchantUid={}", impUid, merchantUid);
//            throw new BasicException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
//        }
//
//        // 3. 결제 위변조 최종 검증 및 Payment 엔티티 업데이트
//        validateAndProcessPayment(payment, portonePaymentData); // 이 메서드 내에서 결제 상태 업데이트 및 저장을 처리합니다.
//
//        // 4. 모든 검증 성공: Reservation 정보 업데이트
//        Reservation reservation = payment.getReservation();
//        if (reservation != null) {
//            // PEDING_APPROVAL 결제 후 사장 승인 대기 중 상태
//            reservation.setStatus("PENDING_APPROVAL"); // Reservation 엔티티의 status 타입에 맞게 설정
//            reservationRepository.save(reservation);
//            log.info("Reservation 엔티티 상태 업데이트 (PENDING_APPROVAL): reservationId={}", reservation.getReservationId());
//        }
//
//        // 응답 DTO를 PaymentCompleteResDto로 반환
//        PaymentCompleteResDto response = new PaymentCompleteResDto();
//        response.setSuccess(true);
//        response.setMerchantUid(merchantUid);
//        response.setImpUid(impUid);
//        response.setStatus(Enums.PaymentStatus.PAID.name()); // Enum의 이름을 String으로 설정
//        response.setMessage("결제가 성공적으로 처리되었습니다.");
//        log.info("결제 및 예약 정보 DB 저장 완료: impUid={}, merchantUid={}", impUid, merchantUid);
//        return response;
//    }
//
////    @Override
////    @Transactional
////    public com.siot.IamportRestClient.response.Payment cancelPayment(String impUid, BigDecimal amount, String reason) throws BasicException, IamportResponseException, IOException {
////        log.warn("결제 취소 요청 시작 - impUid: {}, 금액: {}, 사유: {}", impUid, amount, reason);
////
////        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = client.paymentByImpUid(impUid);
////        if (iamportResponse.getCode() != 0 || iamportResponse.getResponse() == null) {
////            log.error("아임포트 결제 정보 조회 실패 (취소 대상): {}", iamportResponse.getMessage());
////            throw new BasicException(ErrorCode.PAYMENT_INFO_NOT_FOUND);
////        }
////        com.siot.IamportRestClient.response.Payment targetPayment = iamportResponse.getResponse();
////
////        ReservationPayment localPayment = reservationPaymentRepository.findByImpUid(impUid)
////                .orElseThrow(() -> new BasicException(ErrorCode.NOTFOUNT_MERCHANTUID));
////
////        if (localPayment.getStatus() == Enums.PaymentStatus.CANCELLED) {
////            log.warn("이미 취소된 결제입니다: impUid={}", impUid);
////            return targetPayment;
////        }
////
////        CancelData cancelData = new CancelData(impUid, true, reason);
////        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
////            cancelData.setAmount(amount);
////        }
////
////        IamportResponse<com.siot.IamportRestClient.response.Payment> cancelResponse = client.cancelPaymentByImpUid(cancelData);
////
////        if (cancelResponse.getCode() != 0) {
////            log.error("아임포트 결제 취소 실패: {}", cancelResponse.getMessage());
////            throw new BasicException(ErrorCode.PAYMENT_CANCEL_FAILED);
////        }
////
////        com.siot.IamportRestClient.response.Payment canceledIamportPayment = cancelResponse.getResponse();
////
////        localPayment.setStatus(Enums.PaymentStatus.CANCELLED);
////        localPayment.setCancelledAt(LocalDateTime.now());
////        reservationPaymentRepository.save(localPayment);
////        log.info("DB의 결제 상태를 CANCELLED로 업데이트 완료: impUid={}", impUid);
////
////        Reservation reservation = localPayment.getReservation();
////        if (reservation != null &&
////                (!"APPROVED".equals(reservation.getStatus()) && !"REJECTED".equals(reservation.getStatus()))) {
////            reservation.setStatus("CANCELLED");
////            reservationRepository.save(reservation);
////            log.info("Reservation 엔티티 상태 업데이트 (CANCELLED): reservationId={}", reservation.getReservationId());
////        }
////
////        return canceledIamportPayment;
////    }
//
//
//    // private 메서드는 인터페이스에 정의하지 않고 구현체 내부에 유지합니다.
//    private String generateMerchantUid(Long reservationId) {
//        return "res_" + reservationId + "_" + UUID.randomUUID().toString().substring(0, 8);
//    }
//
//    private void validateAndProcessPayment(
//            ReservationPayment payment,
//            Payment portonePaymentData) {
//
//        if (!portonePaymentData.getStatus().equals("paid")) {
//            log.warn("결제 상태 불일치 (paid 아님): impUid={}, status={}", portonePaymentData.getImpUid(), portonePaymentData.getStatus());
//            payment.setStatus(Enums.PaymentStatus.FAILED);
//            reservationPaymentRepository.save(payment);
//            throw new BasicException(ErrorCode.PAYMENT_NOT_PAID);
//        }
//
//        // DB에 저장된 merchantUid와 PortOne 응답의 merchantUid가 일치하는지 확인
//        // Payment 엔티티의 필드명이 'portoneMerchantUid'라고 가정합니다.
//        if (!portonePaymentData.getMerchantUid().equals(payment.getMerchantUid())) { // payment.getPortoneMerchantUid()를 사용
//            log.error("Merchant UID 불일치: PortOne={}, DB={}", portonePaymentData.getMerchantUid(), payment.getMerchantUid());
//            payment.setStatus(Enums.PaymentStatus.FAILED);
//            reservationPaymentRepository.save(payment);
//            throw new BasicException(ErrorCode.PAYMENT_MERCHANT_UID_MISMATCH);
//        }
//
//        // 금액 비교는 BigDecimal.compareTo()를 사용하여 정확하게 수행
//        if (portonePaymentData.getAmount().compareTo(new BigDecimal(payment.getAmount())) != 0) {
//            log.error("결제 금액 불일치: PortOne={}, Expected={}", portonePaymentData.getAmount(), payment.getAmount());
//            payment.setStatus(Enums.PaymentStatus.FAILED);
//            reservationPaymentRepository.save(payment);
//            throw new BasicException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
//        }
//
//        // PortOne에서 받은 impUid를 Payment 엔티티에 저장 (DB 컬럼이 portone_imp_uid라면 엔티티 필드도 portoneImpUid가 적합)
//        payment.setImpUid(portonePaymentData.getImpUid()); // Payment 엔티티에 setPortoneImpUid가 있다고 가정
//        payment.setPaidAt(LocalDateTime.now()); // 결제 완료 시간 설정
//        payment.setStatus(Enums.PaymentStatus.PAID); // 상태를 PAID로 변경
//        reservationPaymentRepository.save(payment); // 업데이트된 Payment 엔티티 저장
//
//        log.info("결제 유효성 검증 성공 및 Payment 엔티티 업데이트 완료: paymentId={}, impUid={}", payment.getPaymentId(), portonePaymentData.getImpUid());
//    }
//}