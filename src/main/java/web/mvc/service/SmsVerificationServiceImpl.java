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

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsVerificationServiceImpl implements SmsVerificationService {

    private final SmsVerificationRepository smsRepository;
    private final CoolsmsProperties props;
    private DefaultMessageService messageService;
    private final Random random = new Random();

    // Nurigo SDK 초기화
    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(
                props.getKey(), props.getSecret(), "https://api.coolsms.co.kr");
        log.info("Nurigo SDK 초기화 완료");
    }


    @Override
    @Transactional
    public void sendVerificationCode(String phone, String purpose) {
        log.info("messageService = {}", messageService);
        // 1) 6자리 랜덤 코드 생성
        String code = String.format("%06d", random.nextInt(1_000_000));
        log.info("생성된 SMS 인증코드: {}", code);
        // 2) 만료시간: 5분 후
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(5);
        // 3) DB 저장
        smsRepository.save(SmsVerification.builder()
                .phone(phone)
                .code(code)
                .purpose(purpose)
                .verified(false)
                .expireAt(expireAt)
                .build());

        // 4) 문자 발송 메소드 호출
        sendSms(phone, code);
    }

    /** 문자 발송 */
    private void sendSms(String to, String code) {

        Message message = new Message();
        message.setFrom(props.getFromNumber()); // 발신자 번호
        message.setTo(to);                      // 수신자 번호
        message.setText("[MatZip] 인증번호 [" + code + "]를 입력해주세요."); // 인증코드

        SingleMessageSendingRequest request = new SingleMessageSendingRequest(message);

        try {
            // 단일 메시지 전송
            SingleMessageSentResponse response = messageService.sendOne(request);
            log.info("SMS 발송 성공, 응답 = {}", response);
        } catch (Exception e) {
            log.error("SMS 전송 실패", e);
            throw new BasicException(ErrorCode.SMS_SENDING_FAILED);
        }
    }


    @Override
    @Transactional
    public void verifyCode(String phone, String code, String purpose) {
        // 가장 최근 인증 기록 조회
        SmsVerification ver = smsRepository.findLatestVerification(phone, purpose)
                .orElseThrow(() -> new BasicException(ErrorCode.VERIFICATION_NOT_FOUND));

        // 중복 인증 방지
        if (ver.getVerified()) {
            throw new BasicException(ErrorCode.ALREADY_VERIFIED);
        }
        // 만료 시간 검증
        if (ver.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BasicException(ErrorCode.EXPIRED_CODE);
        }
        // 코드 일치 여부 검증
        if (!ver.getCode().equals(code)) {
            throw new BasicException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 인증완료 처리
        smsRepository.verifySmsById(ver.getSmsId());
    }

}
