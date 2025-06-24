//package web.mvc.service;
//
//import web.mvc.domain.Reservation;
//import web.mvc.dto.PreparationReqDto;
//
//public interface ReservationService {
//
//    // 사용자로부터 예약 신청을 시작
//    Reservation initiateReservation(PreparationReqDto requestDto);
//    // PortOne 웹훅 콜백을 처리하고 결제를 검증
//    boolean handlePortOneWebhook(PortOneWebhookDto webhookDto);
//    // 사장이 예약 요청을 승인하거나 거절
//    Reservation handleOwnerAction(Long reservationId, OwnerActionRequestDto actionDto);
//    // 특정 예약 정보를 조회
//    Reservation getReservationById(Long reservationId);
//}
