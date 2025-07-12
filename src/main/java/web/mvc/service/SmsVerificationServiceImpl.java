package web.mvc.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import web.mvc.config.CoolsmsProperties;
import web.mvc.domain.SmsVerification;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.SmsVerificationRepository;
import web.mvc.repository.UserRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class SmsVerificationServiceImpl implements SmsVerificationService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SmsVerificationRepository smsRepository;
    @Autowired
    private CoolsmsProperties props;
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    private RestTemplate restTemplate;

    private final Random random = new Random();

    // Nurigo SDK 초기화
    @PostConstruct
    public void init() {
        this.restTemplate = restTemplateBuilder.build();
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
        System.out.println("[MatZIP] 인증번호 [" + code + "]를 입력해주세요.");
        // 문자 발송 메소드 호출 (배포시 주석 풀기)
        //sendSms(phone, code);
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
    // SDK 호출하던 sendSms()를 RestTemplate 호출로 대체
    private void sendSms(String to, String code) {
        String url = "https://api.coolsms.co.kr/sms/2/send";  // v2 SMS API 엔드포인트

        long timestamp = Instant.now().getEpochSecond();
        String salt = generateSalt();
        String signature = generateSignature(timestamp, salt); // HMAC-MD5(signature)

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("api_key", props.getKey());
        body.add("timestamp", String.valueOf(timestamp));
        body.add("salt", salt);
        body.add("signature", signature);
        body.add("to", to);
        body.add("from", props.getFromNumber());
        body.add("text", "[MatZIP] 인증번호 [" + code + "]를 입력해주세요.");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(url, request, String.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new BasicException(ErrorCode.SMS_SENDING_FAILED);
            }
        } catch (RestClientException e) {
            throw new BasicException(ErrorCode.SMS_SENDING_FAILED);
        }
    }

    // salt 생성 (랜덤 20바이트를 hex 문자열로)
    private String generateSalt() {
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // HMAC-MD5(signature) 생성
    private String generateSignature(long timestamp, String salt) {
        try {
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(new SecretKeySpec(props.getSecret().getBytes(StandardCharsets.UTF_8), "HmacMD5"));
            byte[] hash = mac.doFinal((timestamp + salt).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
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
