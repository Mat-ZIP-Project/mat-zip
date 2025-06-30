package web.mvc.service;

import web.mvc.domain.Review;
import web.mvc.dto.ReservationDetailDto;
import web.mvc.exception.BasicException;

import java.util.List;

public interface MyPageService {

    /**
     * 사용자의 전체 예약 내력 조회
     */
    List<ReservationDetailDto> getUserReservations(Long id) throws BasicException;

    /**
     * 사용자의 전체 리뷰 내역 조회
     */
    List<Review> getUserReviews(Long id) throws BasicException;

    /**
     * 사용자의 전체 모임 내역 조회
     */
//    List<Group> getUserGroups(Long id) throws BasicException;

    ReservationDetailDto cancelReservation(Long id, Long reservationId) throws BasicException;

}
