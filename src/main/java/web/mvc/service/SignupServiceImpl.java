package web.mvc.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import web.mvc.domain.OwnerInfo;
import web.mvc.domain.Restaurant;
import web.mvc.domain.User;
import web.mvc.dto.AddressResponse;
import web.mvc.dto.SignupOwnerRequest;
import web.mvc.dto.SignupRequest;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.OwnerInfoRepository;
import web.mvc.repository.RestaurantRepository;
import web.mvc.repository.SmsVerificationRepository;
import web.mvc.repository.UserRepository;

import java.sql.Time;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupServiceImpl implements SignupService {

    private final UserRepository userRepository;
    private final OwnerInfoRepository ownerInfoRepository;
    private final SmsVerificationRepository smsRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final ModelMapper modelMapper;

    @Value("${kakao.map.rest-api-key}")
    private String kakaoApiKey;
    @Value("${kakao.map.base-url}")
    private String kakaoBaseUrl;
    @Value("${nts.api.service-key}")
    private String ntsServiceKey;
    private static final String NTS_API_URL = "https://api.odcloud.kr/api/nts-businessman/v1/status";
    private static final Set<String> VALID_CATEGORIES = Set.of("한식", "양식", "중식", "일식", "카페");


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

    /** 비밀번호 유효성 검증 */
    @Override
    public void validatePassword(String password) {
        if (password == null || password.length() < 10) {
            throw new BasicException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        // 영문 대문자, 소문자, 숫자, 특수문자 중 2종류 이상 조합 검증
        long typeCount = Stream.of(
                Pattern.compile("[a-z]"), Pattern.compile("[A-Z]"),
                Pattern.compile("[0-9]"), Pattern.compile("[!@#$%^&*(),.?\":{}|<>]")
        ).mapToLong(pattern -> pattern.matcher(password).find() ? 1 : 0).sum();

        if (typeCount < 2) {
            throw new BasicException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
    }

    @Override
    public void verifyBusinessNumber(String businessNumber) {
        // DB 중복체크
        if (ownerInfoRepository.existsByBusinessNumber(businessNumber)) {
            throw new BasicException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
        }

        // 국세청 API로 사업자등록번호 유효성 검증
        String cleanBusinessNumber = businessNumber.replaceAll("-", "");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Body에 JSON 형태로 b_no 배열 전달
        Map<String, Object> requestBody = Map.of("b_no", new String[]{cleanBusinessNumber});
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        // API 호출
        String url = NTS_API_URL + "?serviceKey=" + ntsServiceKey;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new BasicException(ErrorCode.BUSINESS_API_ERROR);
        }

        JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
        if (!jsonResponse.has("data") || jsonResponse.getAsJsonArray("data").size() == 0) {
            throw new BasicException(ErrorCode.INVALID_BUSINESS_NUMBER);
        }

        JsonObject data = jsonResponse.getAsJsonArray("data").get(0).getAsJsonObject();
        if (!"계속사업자".equals(data.get("b_stt").getAsString())) {
            throw new BasicException(ErrorCode.INVALID_BUSINESS_NUMBER);
        }
    }

    @Override
    public void checkSmsVerified(String phone, String purpose) {
        smsRepository.findLatestVerifiedSms(phone, purpose)
                .orElseThrow(() -> new BasicException(ErrorCode.SMS_VERIFICATION_REQUIRED));
    }

    @Override
    public List<AddressResponse> searchAddress(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = kakaoBaseUrl + "/v2/local/search/address.json?query={query}&size=10";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, query);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BasicException(ErrorCode.ADDRESS_SEARCH_FAILED);
        }

        List<AddressResponse> addressList = new ArrayList<>();
        JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray documents = jsonResponse.getAsJsonArray("documents");

        for (int i = 0; i < documents.size(); i++) {
            JsonObject document = documents.get(i).getAsJsonObject();
            JsonObject address = document.getAsJsonObject("address");

            if (address != null) {
                AddressResponse addressResponse = AddressResponse.builder()
                        .addressName(address.get("address_name").getAsString())
                        .regionSido(address.get("region_1depth_name").getAsString())
                        .regionSigungu(address.get("region_2depth_name").getAsString())
                        .latitude(Double.parseDouble(document.get("y").getAsString()))
                        .longitude(Double.parseDouble(document.get("x").getAsString()))
                        .build();
                addressList.add(addressResponse);
            }
        }
        return addressList;
    }


    @Override
    @Transactional
    public void signupUser(SignupRequest request) {
        // 필수 기본 필드 검증
        validateCommonUserInfo(request.getUserId(), request.getPhone(), request.getPassword(),
                request.getTermsAgreed(), request.getPrivacyAgreed());
        validatePreferenceCategory(request.getPreferenceCategory());

        // SMS 인증 확인
        checkSmsVerified(request.getPhone(), "SIGNUP");

        // 사용자 생성 및 저장
        User user = createUser(request);
        userRepository.save(user);
        log.info("일반 사용자 회원가입 완료: {}", request.getUserId());
    }

    @Override
    @Transactional
    public void signupOwner(SignupOwnerRequest request) {
        // 필수 기본 필드 검증
        validateCommonUserInfo(request.getUserId(), request.getPhone(), request.getPassword(),
                request.getTermsAgreed(), request.getPrivacyAgreed());
        checkSmsVerified(request.getPhone(), "SIGNUP");

        // 사업자 등록번호 검증
        verifyBusinessNumber(request.getBusinessNumber());
        // 식당 정보 검증
        validateRestaurantInfo(request);

        // 저장
        User savedUser = createUser(request);
        userRepository.save(savedUser);

        OwnerInfo ownerInfo = OwnerInfo.builder()
                .businessNumber(request.getBusinessNumber())
                .user(savedUser)
                .build();
        ownerInfoRepository.save(ownerInfo);

        Restaurant restaurant = createRestaurant(request, ownerInfo);
        restaurantRepository.save(restaurant);

        log.info("식당주 회원가입 완료: {}", request.getUserId());
    }


    ///////// private 메소드 ///////////////////////////////////////////////////////////////////
    /** 필수 기본 필드 검증 */
    private void validateCommonUserInfo(String userId, String phone, String password,
                                        Boolean termsAgreed, Boolean privacyAgreed) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new BasicException(ErrorCode.USER_NOT_FOUND);
        }
        checkUserIdDuplicate(userId);
        checkPhoneDuplicate(phone);
        validatePassword(password);

        if (!Boolean.TRUE.equals(termsAgreed) || !Boolean.TRUE.equals(privacyAgreed)) {
            throw new BasicException(ErrorCode.TERMS_NOT_AGREED);
        }
    }

    /**
     * 선호 카테고리 유효성 검증
     * - (한식,양식,중식,일식,카페)에서 최대 2개까지만 선택 가능
     */
    private void validatePreferenceCategory(String preferenceCategory) {
        // 일반 사용자는 선호 카테고리가 필수는 아님 (선택사항)
        if (preferenceCategory == null || preferenceCategory.trim().isEmpty()) {
            return;
        }

        // 콤마로 분리하여 카테고리 목록 생성
        List<String> categories = Arrays.stream(preferenceCategory.split(","))
                .map(String::trim).filter(category -> !category.isEmpty()).toList();

        // 최대 2개까지만 허용
        if (categories.size() > 2) {
            throw new BasicException(ErrorCode.INVALID_PREFERENCE_CATEGORY);
        }

        // 각 카테고리가 유효한지 검증
        categories.forEach(category -> {
            if (!VALID_CATEGORIES.contains(category)) {
                throw new BasicException(ErrorCode.INVALID_PREFERENCE_CATEGORY);
            }
        });
        log.info("선호 카테고리 검증 완료: {}", preferenceCategory);
    }

    /** 식당 정보 검증 */
    private void validateRestaurantInfo(SignupOwnerRequest request) {
        if (!VALID_CATEGORIES.contains(request.getCategory())) {
            throw new BasicException(ErrorCode.INVALID_RESTAURANT_CATEGORY);
        }
        if (request.getMaxWaitingLimit() == null || request.getMaxWaitingLimit() < 0) {
            throw new BasicException(ErrorCode.INVALID_WAITING_LIMIT);
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
                .preferenceCategory(request.getPreferenceCategory())
                .build();
    }
    /** 식당주 용 user 객체 저장 */
    private User createUser(SignupOwnerRequest request) {
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

    /** 식당 정보 DB저장 */
    private Restaurant createRestaurant(SignupOwnerRequest request, OwnerInfo ownerInfo) {
        List<AddressResponse> addresses = searchAddress(request.getAddress());
        if (addresses.isEmpty()) {
            throw new BasicException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        AddressResponse addressInfo = addresses.get(0);

        return Restaurant.builder()
                .owner(ownerInfo)
                .restaurantName(request.getRestaurantName())
                .address(addressInfo.getAddressName())
                .regionSido(addressInfo.getRegionSido())
                .regionSigungu(addressInfo.getRegionSigungu())
                .latitude(addressInfo.getLatitude())
                .longitude(addressInfo.getLongitude())
                .phone(request.getRestaurantPhone())
                .category(request.getCategory())
                .descript(request.getDescript())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .maxWaitingLimit(request.getMaxWaitingLimit())
                .build();
    }

}
