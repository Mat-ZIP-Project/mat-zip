package web.mvc.service;

import com.siot.IamportRestClient.response.Payment;
import web.mvc.domain.User;
import web.mvc.dto.NoShowReservationDto;
import web.mvc.dto.PendingReservationDto;
import web.mvc.dto.ReservationCreateReqDto;
import web.mvc.dto.ReservationCreateResDto;
import web.mvc.exception.BasicException;

import java.math.BigDecimal;
import java.util.List;

public interface ReservationService {

    ReservationCreateResDto createReservation(User user, ReservationCreateReqDto request) throws BasicException;
    /**
     *  예약 상태를 업데이트하고, 상태 변경에 따라 사용자에게 알림을 전송
     *  식당 점주가 예약을 승인하거나 거절할 때 호출된다.
     */
    void updateReservationStatus(Long reservationId, String newStatus, String ownerNotes) throws BasicException;

    /** 결제완료 된 예약 대기 명단 리스트 */
    List<PendingReservationDto> getPendingReservations(String ownerUserId);

    /** 노쇼처리 대상 리스트 */
    List<NoShowReservationDto> getNoShowCandidates(String ownerUserId);

    /** 노쇼 처리 */
    void markNoShow(Long reservationId) throws BasicException;
}
