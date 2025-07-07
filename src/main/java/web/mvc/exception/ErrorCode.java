package web.mvc.exception;

import com.google.api.Http;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
/**
 * BAD_REQUEST      - 400, 잘못된 입력값
 * UNAUTHORIZED     - 401, 로그인 필요
 * PAYMENT_REQUIRED - 402, 결제 필요
 * FORBIDDEN        - 403, 권한 없음
 * NOT_FOUND        - 404, 리소스 없음
 * REQUEST_TIMEOUT  - 408, 요청 시간 초과
 * CONFLICT         - 409, 충돌 (중복데이터, 데이터 불일치 등)
 * */
public enum ErrorCode { //enum은 'Enumeration' 의 약자로 열거, 목록 이라는 뜻

    // 회원가입 관련 예외처리
    DUPLICATE_USER_ID(HttpStatus.CONFLICT, "Duplicate user id", "이미 사용중인 아이디입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "Duplicate phone", "이미 등록된 휴대폰번호입니다."),
    DUPLICATE_BUSINESS_NUMBER(HttpStatus.CONFLICT, "Duplicate business number", "이미 등록된 사업자등록번호입니다."),
    SMS_VERIFICATION_REQUIRED(HttpStatus.BAD_REQUEST, "SMS verification required", "휴대폰 인증이 필요합니다."),
    INVALID_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST, "Invalid business number", "유효하지 않은 사업자등록번호입니다."),
    BUSINESS_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Business API error", "사업자등록번호 검증 중 오류가 발생했습니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "Invalid password format", "비밀번호는 최소 10자리, 영문 대소문자/숫자/특수문자 중 2종류 이상 조합해야 합니다."),
    TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "Terms not agreed", "이용약관 및 개인정보처리방침에 동의해야 합니다."),
    INVALID_PREFERENCE_CATEGORY(HttpStatus.BAD_REQUEST, "Invalid preference category", "유효하지 않은 선호 카테고리입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "Password mismatch", "비밀번호가 일치하지 않습니다."),

    // 로그인 관련 예외처리
    WRONG_PASS( HttpStatus.BAD_REQUEST, "Password wrong","비밀번호 오류입니다."),
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "Access denied","로그인하고 이용해주세요."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"Invalid access token", "토큰이 만료되었습니다."),
    REFRESH_NOT_FOUND(HttpStatus.UNAUTHORIZED,"Invalid refresh token",  "refresh 토큰이 만료되었습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"Not found userId", "사용자를 찾을 수 없습니다."),

    // 현지인 인증 관련 예외처리
    FAILED_AUTH(HttpStatus.BAD_REQUEST, "Auth failed", "인증 실패입니다."),
    DUPLICATE_DATE(HttpStatus.CONFLICT, "Duplicate date", "하루에 한번만 인증 가능합니다. 내일 재인증 해주세요."),
    NO_AUTH_LOGS(HttpStatus.NOT_FOUND, "Not found Auth logs", "인증 기록이 없습니다."),

    // 식당 관련 예외처리
    RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND,"Restaurant Not found", "식당을 찾을 수 없습니다."),
    ALREADY_LIKED(HttpStatus.CONFLICT, "Duplcate like", "이미 찜한 식당입니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "Not found Like", "찜 내역이 없습니다."),
    ADDRESS_SEARCH_FAILED(HttpStatus.BAD_REQUEST, "Address search failed", "주소 검색에 실패했습니다."),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "Address not found", "해당 주소를 찾을 수 없습니다."),
    INVALID_RESTAURANT_CATEGORY(HttpStatus.BAD_REQUEST, "Invalid restaurant category", "유효하지 않은 식당 카테고리입니다."),
    INVALID_WAITING_LIMIT(HttpStatus.BAD_REQUEST, "Invalid waiting limit", "최대 대기 한도는 0 이상이어야 합니다."),

    // SMS 인증 관련 예외처리
    SMS_SENDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed SMS sending", "문자 발송에 실패했습니다."),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Not found Verification data", "인증 기록이 없습니다."),
    ALREADY_VERIFIED(HttpStatus.CONFLICT, "Already Verified sms code", "이미 인증된 코드입니다."),
    EXPIRED_CODE(HttpStatus.REQUEST_TIMEOUT, "Expired verification code", "인증 코드가 만료되었습니다."),
    INVALID_VERIFICATION_CODE( HttpStatus.BAD_REQUEST, "Invalid verification code", "인증 코드가 일치하지 않습니다."),
    PHONE_NOT_FOUND(HttpStatus.NOT_FOUND, "Phone not found", "등록되지 않은 휴대폰번호입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request", "잘못된 요청입니다."),

    // --- PortOne 결제 관련 오류 코드 추가 ---
    PAYMENT_PREPARE_FAILED(HttpStatus.BAD_REQUEST, "PAYMENT_PREPARE_FAILED","결제 사전 검증에 실패했습니다."),
    PAYMENT_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_INFO_NOT_FOUND","PortOne에서 결제 정보를 조회할 수 없습니다."),
    PAYMENT_NOT_PAID(HttpStatus.BAD_REQUEST, "","결제가 완료되지 않았습니다."),
    PAYMENT_MERCHANT_UID_MISMATCH(HttpStatus.BAD_REQUEST,"", "주문 번호가 일치하지 않습니다. 위변조 가능성."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.NOT_FOUND, "","결제 금액이 일치하지 않습니다. 위변조 가능성."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "","이미 처리된 결제입니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "","해당 예약을 찾을 수 없습니다."),
    PAYMENT_DB_ERROR(HttpStatus.BAD_REQUEST, "","결제 정보를 DB에 저장하는 중 오류가 발생했습니다."),
    PAYMENT_ALREADY_PENDING(HttpStatus.BAD_REQUEST, "","이미 진행 중인 결제가 있습니다."), // 선택적, 중복 사전 검증 방지
    PAYMENT_CANCEL_FAILED(HttpStatus.BAD_REQUEST, "","결제 취소에 실패했습니다."),
    // --- 기존 오류 코드 ---
    NOTFOUNT_MERCHANTUID(HttpStatus.NOT_FOUND, "","결제 정보를 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "","예약에 대한 취소 권한이 없습니다."),
    INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST,"","예약을 취소할 수 없습니다."),


    //커스텀 코스 예외처리
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND,"","해당 코스가 존재하지 않습니다."),

    INVALID_RESERVATION_STATUS_TRANSITION(HttpStatus.BAD_REQUEST,"","현재 예약 상태에서 해당 상태로 변경할 수 없습니다."),

    // 테스트
    FORBIDDEN(HttpStatus.FORBIDDEN, "", "FORBIDDEN"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "", "INVALID_INPUT"),

    // Waiting 관련 에러코드 추가
    WAITING_ALREADY_EXISTS(HttpStatus.CONFLICT, "Waiting Exists", "이미 입장 대기 중인 웨이팅이 존재합니다."),
    WAITING_NOT_FOUND(HttpStatus.NOT_FOUND, "Waiting Not Found", "웨이팅 정보를 찾을 수 없습니다."),
    INVALID_STATUS_CHANGE(HttpStatus.BAD_REQUEST, "INVALID_STATUS_CHANGE", "웨이팅 상태가 변경할 수 없는 상황입니다."),
    NOT_EXPIRED_YET(HttpStatus.BAD_REQUEST, "NOT_EXPIRED_YET", "아직 15분이 다 경과하지 않았습니다."),
    WAITING_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "WaitingStatus Not Found", "waiting_status가 존재하지 않습니다.");


    private final HttpStatus httpStatus;
    private  final String title;
    private final String message;
}