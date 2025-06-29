package web.mvc.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import web.mvc.domain.OwnerInfo;
import web.mvc.domain.User;
import web.mvc.dto.SignupRequest;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.OwnerInfoRepository;
import web.mvc.repository.SmsVerificationRepository;
import web.mvc.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupServiceImpl implements SignupService {

    private final UserRepository userRepository;
    private final OwnerInfoRepository ownerInfoRepository;
    private final SmsVerificationRepository smsRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${nts.api.service-key}")
    private String ntsServiceKey;

    private static final String NTS_API_URL = "https://api.odcloud.kr/api/nts-businessman/v1/status";
    private static final String SERVICE_KEY = "";

    @Override
    public void checkUserIdDuplicate(String userId) {
        if (userRepository.existsByUserId(userId)) {
            throw new BasicException(ErrorCode.DUPLICATE_USER_ID);
        }
    }

    @Override
    public void checkPhoneDuplicate(String phone) {
        if (userRepository.existsByPhone(phone)) {
            throw new BasicException(ErrorCode.DUPLICATE_PHONE);
        }
    }

    @Override
    public void verifyBusinessNumber(String businessNumber) {
        // DB 중복체크
        checkBusinessNumberDuplicate(businessNumber);

        // 국세청 API로 사업자등록번호 유효성 검증
        validateBusinessNumberWithAPI(businessNumber);
    }

    @Override
    @Transactional
    public void signupUser(SignupRequest request) {
        // 유효성 검증
        validateSignupRequest(request);

        // SMS 인증 확인
        checkSmsVerified(request.getPhone(), "SIGNUP");

        // 사용자 생성 및 저장
        User user = createUser(request);
        userRepository.save(user);
        log.info("일반 사용자 회원가입 완료: {}", request.getUserId());
    }

    @Override
    @Transactional
    public void signupOwner(SignupRequest request) {
        // 유효성 검증
        validateSignupRequest(request);

        // 사업자번호 이중 검증
        if (request.getBusinessNumber() == null || request.getBusinessNumber().trim().isEmpty()) {
            throw new BasicException(ErrorCode.INVALID_BUSINESS_NUMBER);
        }
        verifyBusinessNumber(request.getBusinessNumber());

        // SMS 인증 확인
        checkSmsVerified(request.getPhone(), "SIGNUP");

        User user = createUser(request);
        User savedUser = userRepository.save(user);

        // 사업자 정보 저장
        OwnerInfo ownerInfo = OwnerInfo.builder()
                .businessNumber(request.getBusinessNumber())
                .user(savedUser)
                .build();
        ownerInfoRepository.save(ownerInfo);

        log.info("식당주인 회원가입 완료: {}", request.getUserId());
    }

    @Override
    public void checkSmsVerified(String phone, String purpose) {
        smsRepository.findLatestVerifiedSms(phone, purpose)
                .orElseThrow(() -> new BasicException(ErrorCode.SMS_VERIFICATION_REQUIRED));
    }

    ///////// private 메소드 ///////////////////////////////////////////////////////////////////
    /** 비밀번호 유효성 검증 */
    private void validatePassword(String password) {
        if (password == null || password.length() < 10) {
            throw new BasicException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        // 영문 대문자, 소문자, 숫자, 특수문자 중 2종류 이상 조합 검증
        long typeCount = Stream.of(
                Pattern.compile("[a-z]"),
                Pattern.compile("[A-Z]"),
                Pattern.compile("[0-9]"),
                Pattern.compile("[!@#$%^&*(),.?\":{}|<>]")
        ).mapToLong(pattern -> pattern.matcher(password).find() ? 1 : 0).sum();

        if (typeCount < 2) {
            throw new BasicException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
    }


    /** 사업자 번호 DB 중복체크 */
    private void checkBusinessNumberDuplicate(String businessNumber) {
        if (ownerInfoRepository.existsByBusinessNumber(businessNumber)) {
            throw new BasicException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
        }
    }

    /**
     * 국세청 API를 통한 사업자등록번호 검증
     */
    private void validateBusinessNumberWithAPI(String businessNumber) {
        String cleanBusinessNumber = businessNumber.replaceAll("-", "");
        //HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Body에 JSON 형태로 b_no 배열 전달
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("b_no", new String[]{cleanBusinessNumber});

        //HTTP 요청 생성
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // API 호출
        //String url = NTS_API_URL + "?serviceKey=" + SERVICE_KEY;
        String url = NTS_API_URL + "?serviceKey=" + ntsServiceKey;
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new BasicException(ErrorCode.BUSINESS_API_ERROR);
        }

        JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
        if (!jsonResponse.has("data") || jsonResponse.getAsJsonArray("data").size() == 0) {
            throw new BasicException(ErrorCode.INVALID_BUSINESS_NUMBER);
        }

        JsonObject data = jsonResponse.getAsJsonArray("data").get(0).getAsJsonObject();
        String status = data.get("b_stt").getAsString();

        // 계속사업자인 경우만 유효한 것으로 판단
        if (!"계속사업자".equals(status)) {
            throw new BasicException(ErrorCode.INVALID_BUSINESS_NUMBER);
        }
    }


    /** 필수 기본 필드 검증 */
    private void validateSignupRequest(SignupRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new BasicException(ErrorCode.USER_NOT_FOUND);
        }

        // 중복 체크 (아이디, 휴대폰번호)
        checkUserIdDuplicate(request.getUserId());
        checkPhoneDuplicate(request.getPhone());

        // 비밀번호 검증
        validatePassword(request.getPassword());

        // 약관 동의 검증
        if (!Boolean.TRUE.equals(request.getTermsAgreed()) ||
            !Boolean.TRUE.equals(request.getPrivacyAgreed())) {
            throw new BasicException(ErrorCode.TERMS_NOT_AGREED);
        }
    }

    /** 회원가입 시 사용자 정보 저장용 객체 생성 */
    private User createUser(SignupRequest request) {
        return User.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole())
                .userStatus("활성")
                .userGrade("새싹")
                .pointBalance(0)
                .noShow(false)
                .gpsVerified(false)
                .termsAgreed(request.getTermsAgreed())
                .privacyAgreed(request.getPrivacyAgreed())
                .build();
    }
}
