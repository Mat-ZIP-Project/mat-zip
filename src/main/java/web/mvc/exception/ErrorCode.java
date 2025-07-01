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
    DUPLICATED(HttpStatus.CONFLICT , "Duplicate Id", "아이디가 중복입니다."),

    // 로그인 관련 예외처리
    WRONG_PASS( HttpStatus.BAD_REQUEST, "Password wrong","비밀번호 오류입니다.."),
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


