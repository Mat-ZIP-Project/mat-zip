package web.mvc.service;

import web.mvc.domain.*;
import web.mvc.dto.*;
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
    List<ReviewDetailDto> getUserReviews(Long id) throws BasicException;

    /**
     * 사용자의 참여한 모임 내역 조회
     */
    List<ParticipantDetailDto> getParticipatedMeetings(Long id) throws BasicException;

    /**
     * 사용자가 모임에 대해 작성한 리뷰 내역 조회
     */
    List<MeetingReviewDetailDto> getMeetingReviews(Long id) throws BasicException;

    /**
     *  사용자가 생성한 모임 내역 조회
     */
    List<CreatedMeetingDetailDto> getMeeting(Long id) throws BasicException;

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

    /**
     *  사용자의 알림 내역 조회
     */
    List<Notification> getUserNotification(Long id) throws BasicException;

    /**
     *  사용자의 알림을 읽음 상태로 변경
     */
    void markNotificationAsRead(Long id) throws BasicException;
}
