package web.mvc.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
/**
 * Enum(열거형)은 서로 관련된 상수들을 정의하여 편리하게 사용하기 위한 자료형이다. 
 * https://jddng.tistory.com/305
 * 
 * */
public enum ErrorCode { //enum은 'Enumeration' 의 약자로 열거, 목록 이라는 뜻

    DUPLICATED(HttpStatus.CONFLICT , "Duplicate Id", "아이디가 중복입니다."),
    WRONG_PASS( HttpStatus.BAD_REQUEST, "password wrong","비밀번호 오류입니다.."),

    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "Access denied","로그인하고 이용해주세요."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"Invalid access token", "토큰이 만료되었습니다."),
    REFRESH_NOT_FOUND(HttpStatus.UNAUTHORIZED,"Invalid refresh token",  "refresh 토큰이 만료되었습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"Not found userId", "사용자를 찾을 수 없습니다."),

    UPDATE_FAILED(HttpStatus.BAD_REQUEST, "Update fail","수정할수 없습니다."),
    DELETE_FAILED( HttpStatus.BAD_REQUEST, "Delete fail","삭제할 수 없습니다."),
    INSERT_FAILED( HttpStatus.BAD_REQUEST, "Insert fail","등록할 수 없습니다.");


    private final HttpStatus httpStatus;
    private  final String title;
    private final String message;
}


