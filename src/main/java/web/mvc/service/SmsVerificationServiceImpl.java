package web.mvc.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.config.CoolsmsProperties;
import web.mvc.domain.SmsVerification;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.SmsVerificationRepository;
import web.mvc.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsVerificationServiceImpl implements SmsVerificationService {

    private final UserRepository userRepository;
    private final SmsVerificationRepository smsRepository;
    private final CoolsmsProperties props;
    private DefaultMessageService messageService;
    private final Random random = new Random();

    // Nurigo SDK 초기화
    @PostConstruct
    public void init() {
        try {
            this.messageService = NurigoApp.INSTANCE.initialize(
                    props.getKey(), props.getSecret(), "https://api.coolsms.co.kr");
            log.info("Nurigo SDK 초기화 완료");
        } catch (Throwable ex) {
        }
    }


    @Override
    @Transactional
    public void sendVerificationCode(String phone, String purpose) {
        log.info("SMS 인증코드 발송 요청 - phone: {}, purpose: {}", phone, purpose);

        // purpose별 휴대폰번호 검증 ('SIGNUP','PASSWORD_RESET','ID_FIND')
        validatePhoneByPurpose(phone, purpose);

        // 인증코드 생성 및 저장
        String code = generateAndSaveVerificationCode(phone, purpose);

        log.info("인증코드 생성 및 저장 완료 - phone: {}, purpose: {}", phone, purpose);
        // 문자 발송 메소드 호출
        sendSms(phone, code);
    }


    /** purpose별 휴대폰번호 검증 */
    private void validatePhoneByPurpose(String phone, String purpose) {
        switch (purpose) {
            case "SIGNUP":
                validatePhoneForSignup(phone);
                break;
            case "ID_FIND":
            case "PASSWORD_RESET":
                validatePhoneForRecovery(phone);
                break;
            default:
                throw new BasicException(ErrorCode.INVALID_REQUEST);
        }
    }

    /**
     * 회원가입용 휴대폰번호 중복체크 - DB에 존재 X
     */
    private void validatePhoneForSignup(String phone) {
        if (userRepository.existsByPhone(phone))
            throw new BasicException(ErrorCode.DUPLICATE_PHONE);
    }

    /**
     * 계정 복구용 휴대폰번호 중복체크 - DB에 존재 O
     */
    private void validatePhoneForRecovery(String phone) {
        if (!userRepository.existsByPhone(phone))
            throw new BasicException(ErrorCode.PHONE_NOT_FOUND);
    }


    /**
     * 인증코드 생성 및 DB 저장
     */
    private String generateAndSaveVerificationCode(String phone, String purpose) {
        // 6자리 랜덤 코드 생성
        String code = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(5); // 만료시간 5분
        // DB 저장
        smsRepository.save(SmsVerification.builder()
                .phone(phone)
                .code(code)
                .purpose(purpose)
                .verified(false)
                .expireAt(expireAt)
                .build());

        log.info("인증코드 생성 및 저장 완료 - phone: {}, purpose: {}", phone, purpose);
        return code;
    }

    /** 문자 발송 */
    private void sendSms(String to, String code) {

        Message message = new Message();
        message.setFrom(props.getFromNumber()); // 발신자 번호
        message.setTo(to);                      // 수신자 번호
        message.setText("[MatZip] 인증번호 [" + code + "]를 입력해주세요."); // 인증코드

        SingleMessageSendingRequest request = new SingleMessageSendingRequest(message);

        try {
            SingleMessageSentResponse response = messageService.sendOne(request);
            log.info("SMS 발송 성공 - phone: {}, response: {}", to, response);
        } catch (Exception e) {
            log.error("SMS 전송 실패", e);
            throw new BasicException(ErrorCode.SMS_SENDING_FAILED);
        }
    }


    @Override
    @Transactional
    public void verifyCode(String phone, String code, String purpose) {
        // 가장 최근 인증 기록 조회
        SmsVerification sms = smsRepository.findLatestVerification(phone, purpose)
                .orElseThrow(() -> new BasicException(ErrorCode.VERIFICATION_NOT_FOUND));

        // 중복 인증 방지
        if (sms.getVerified()) {
            throw new BasicException(ErrorCode.ALREADY_VERIFIED);
        }
        // 만료 시간 검증
        if (sms.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BasicException(ErrorCode.EXPIRED_CODE);
        }
        // 코드 일치 여부 검증
        if (!sms.getCode().equals(code)) {
            throw new BasicException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 인증완료 처리
        smsRepository.verifySmsById(sms.getSmsId());
    }

}
