package web.mvc.service;

import web.mvc.dto.ReservationCreateReqDto;
import web.mvc.dto.ReservationCreateResDto;
import web.mvc.exception.BasicException;

public interface ReservationService {

    ReservationCreateResDto createReservation(ReservationCreateReqDto request) throws BasicException;
    /**
     *  예약 상태를 업데이트하고, 상태 변경에 따라 사용자에게 알림을 전송
     *  식당 점주가 예약을 승인하거나 거절할 때 호출된다.
     */
    void updateReservationStatus(Long reservationId, String newStatus, String ownerNotes) throws BasicException;
}
