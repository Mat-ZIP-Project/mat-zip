package web.mvc.service;

import web.mvc.domain.*;
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
     * 사용자의 참여한 모임 내역 조회
     */
    List<MeetupParticipant> getParticipatedMeetings(Long id) throws BasicException;

    /**
     *  사용자가 모임에 대해 작성한 리뷰 내역 조회
     */
    List<MeetupReview> getMeetingReviews(Long id) throws BasicException;

    /**
     *  사용자가 생성한 모임 내역 조회
     */
    List<Meeting> getMeeting(Long id) throws BasicException;

    /**
     *  사용자가 예약 취소하는 메서드
     */
    void cancelReservation(Long id, Long reservationId) throws BasicException;

    void checkAndUpdateUserGrade(User user) throws BasicException;

    /**
     *  사용자의 포인트 잔액을 조회
     */
    Integer getUserPointBalance(Long id) throws BasicException;

    /**
     *  사용자의 포인트 내역을 조회
     */
    List<Point> getUserPointHistory(Long id) throws BasicException;

}
