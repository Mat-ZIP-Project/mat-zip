package web.mvc.service;

import web.mvc.domain.*;
import web.mvc.dto.*;
import web.mvc.exception.BasicException;

import java.util.List;

public interface MyPageService {

    /**
     *  사용자의 찜한 식당 내역 조회
     */
    List<RestaurantLikeDetailDto> getUserRestaurantLikes(Long id) throws BasicException;

    /**
     * 사용자의 전체 예약 내역 조회
     */
    List<ReservationDetailDto> getUserReservations(Long id) throws BasicException;

    /**
     * 사용자의 전체 리뷰 내역 조회
     */
    List<ReviewDetailResponse> getUserReviews(Long id) throws BasicException;

    /**
     *  사용자가 예약 취소하는 메서드
     */
    void cancelReservation(Long id, Long reservationId) throws BasicException;

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
    List<NotificationDetailDto> getUserNotification(Long id) throws BasicException;

    /**
     *  사용자의 알림을 읽음 상태로 변경
     */
    void markNotificationAsRead(Long id) throws BasicException;

    /**
     * 알림 isRead의 값이 false인 것 개수
     */
    int getUnreadNotificationCount(Long id) throws BasicException;

    /**
     *  사용자의 선호 카테고리 업데이트
     */
    User updateUserPreference(Long id, UserPreferenceDto userPreferenceDto) throws BasicException;

    /**
     *  리뷰 삭제
     */
    void deleteReview(Long id, Long reviewId) throws BasicException;
}
