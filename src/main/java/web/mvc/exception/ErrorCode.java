package web.mvc.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
/**
 * Enum(열거형)은 서로 관련된 상수들을 정의하여 편리하게 사용하기 위한 자료형이다. 
 * https://jddng.tistory.com/305
 * 
 * */
public enum ErrorCode { //enum은 'Enumeration' 의 약자로 열거, 목록 이라는 뜻
 
	ACCESS_DENIED(600, "로그인하고 이용해주세요."),
	NOTFOUND_ID(601, "존재하지 않는 ID입니다."),
	WRONG_PASS( 602, "비밀번호 오류입니다.."),
	
   FAILED_DETAIL(603, "상세보기 오류입니다."),
   FAILED_UPDATE(604, "글번호 오류로 수정할수 없습니다."),
    NO_AUTH_LOGS(605, "인증 기록이 없습니다."),

    // 식당 관련 예외처리
    RESTAURANT_NOT_FOUND(700, "식당을 찾을 수 없습니다."),
    USER_NOT_FOUND(701, "사용자를 찾을 수 없습니다."),
    ALREADY_LIKED(702, "이미 찜한 식당입니다."),
    LIKE_NOT_FOUND(703, "찜 내역이 없습니다.");
	
  
	
  private final int status;
  private final String msg;
}


