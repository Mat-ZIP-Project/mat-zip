package web.mvc.util;

public class Enums {

    // 예약 상태 Enum
    public enum ReservationStatus {
        PENDING("대기"), // 예약 신청 후 결제 미완료
        PENDING_APPROVAL("결제 완료 - 승인 대기"),
        APPROVED("예약 완료"), // 사장 승인 완료 (최종 확정)
        REJECTED("예약 거절"),  // 사장 거절
        CANCELLED("예약 취소"),   // 사용자 취소
        FAILED("예약 실패");    // 예약 실패

        private final String description;

        ReservationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 결제 상태 Enum (PortOne의 status 필드와 유사하게 사용)
    public enum PaymentStatus {
        PAID("결제 완료"),       // 결제 성공
        READY("결제 대기"),       // 가상계좌 발급 등 결제 대기
        CANCELLED("결제 취소"),   // 결제 취소
        FAILED("결제 실패");     // 결제 실패

        private final String description;

        PaymentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
