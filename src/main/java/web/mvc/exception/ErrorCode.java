package web.mvc.exception;

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
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "Not found Like", "찜 내역이 없습니다.");


    private final HttpStatus httpStatus;
    private  final String title;
    private final String message;
}


