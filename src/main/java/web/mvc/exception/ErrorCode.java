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
   FAILED_DELETE(605, "글을 삭제할수 없습니다.^^."),

    // --- PortOne 결제 관련 오류 코드 추가 ---
    PAYMENT_PREPARE_FAILED(610, "결제 사전 검증에 실패했습니다."),
    PAYMENT_INFO_NOT_FOUND(611, "PortOne에서 결제 정보를 조회할 수 없습니다."),
    PAYMENT_NOT_PAID(612, "결제가 완료되지 않았습니다."),
    PAYMENT_MERCHANT_UID_MISMATCH(613, "주문 번호가 일치하지 않습니다. 위변조 가능성."),
    PAYMENT_AMOUNT_MISMATCH(614, "결제 금액이 일치하지 않습니다. 위변조 가능성."),
    PAYMENT_ALREADY_PROCESSED(615, "이미 처리된 결제입니다."),
    RESERVATION_NOT_FOUND(616, "해당 예약을 찾을 수 없습니다."),
    PAYMENT_DB_ERROR(617, "결제 정보를 DB에 저장하는 중 오류가 발생했습니다."),
    USER_INFO_MISSING(618, "사용자 정보가 누락되었습니다."),
    PAYMENT_ALREADY_PENDING(619, "이미 진행 중인 결제가 있습니다."), // 선택적, 중복 사전 검증 방지
    PAYMENT_CANCEL_FAILED(620, "결제 취소에 실패했습니다."),
    // --- 기존 오류 코드 ---
    NOTFOUNT_MERCHANTUID(606, "결제 정보를 찾을 수 없습니다.");
	
  
	
  private final int status;
  private final String msg;
}


