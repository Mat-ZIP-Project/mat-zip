package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import web.mvc.domain.*;
import web.mvc.dto.*;

import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.MyPageService;
import web.mvc.service.ReviewService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Slf4j
public class MyPageController {

    private final MyPageService myPageService;
    private final ReviewService reviewService;

    /**
     *  사용자의 선호 카테고리 업데이트
     */
    @PostMapping("/update/preference")
    public ResponseEntity<?> updateUserPreference(@AuthenticationPrincipal CustomUserDetails principal, @RequestBody UserPreferenceDto userPreferenceDto) throws BasicException {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            User updatedUser = myPageService.updateUserPreference(id, userPreferenceDto);
            return ResponseEntity.ok(updatedUser);
        } catch (BasicException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     *  사용자의 식당 찜 내역 조회
     */
    @GetMapping("/restaurant/likes")
    public ResponseEntity<List<RestaurantLikeDetailDto>> getUserRestaurantLikes(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            List<RestaurantLikeDetailDto> restaurantLikes = myPageService.getUserRestaurantLikes(id);
            return ResponseEntity.ok(restaurantLikes);
        }  catch (BasicException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    /**
     * 사용자의 전체 예약 내역 조회
     */
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationDetailDto>> getUserReservations(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            List<ReservationDetailDto> reservations = myPageService.getUserReservations(id);
            return ResponseEntity.ok(reservations);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus()).build();
        }
    }

    /**
     *  사용자의 식당 리뷰 내역 조회
     */
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewDetailDto>> getUserReviews(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            List<ReviewDetailDto> reviews = myPageService.getUserReviews(id);
            return ResponseEntity.ok(reviews);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus()).build();
        }
    }

    /**
     * 사용자 예약 취소
     */
    @PostMapping("/reservations/cancel/{reservationId}")
    public ResponseEntity<String> cancelReservation(@AuthenticationPrincipal CustomUserDetails principal,
                                                    @PathVariable Long reservationId) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            myPageService.cancelReservation(id, reservationId);
            log.info("예약 ID 취소 처리가 성공적으로 완료되었습니다.");
            return ResponseEntity.ok("성공");
        } catch (BasicException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(e.getMessage());
        }
    }

    /**
     *  포인트 가져오기
     */
    @GetMapping("/user/point")
    public ResponseEntity<Integer> getUserPoints(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            Integer pointBalance = myPageService.getUserPointBalance(id);
            return ResponseEntity.ok(pointBalance);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus()).build();
        }
    }

    /**
     *  포인트 내역 가져오기
     */
    @GetMapping("/user/points/history")
    public ResponseEntity<List<Point>> getUserPointHistory(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            List<Point> pointHistory = myPageService.getUserPointHistory(id);
            return ResponseEntity.ok(pointHistory);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus()).build();
        }
    }

    /**
     *  사용자의 알림 내역 가져오기
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDetailDto>> getUserNotifications(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            List<NotificationDetailDto> notificationList = myPageService.getUserNotification(id);
            return ResponseEntity.ok(notificationList);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus()).build();
        }
    }

    /**
     *  사용자의 모든 알림을 읽음 상태로 변경
     */
    @PostMapping("/notifications/markAllAsRead")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            myPageService.markNotificationAsRead(id);
            return ResponseEntity.ok().build();
        } catch (BasicException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus()).build();
        }
    }

    /**
     *  읽지 않은 알림 개수 반환
     */
    @GetMapping("/notifications/count")
    public ResponseEntity<Integer> getUserNotificationCount(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            int unreadCount = myPageService.getUnreadNotificationCount(id);
            return ResponseEntity.ok(unreadCount);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus()).build();
        }
    }

    /**
     *  마이페이지에서 사용자가 작성한 리뷰 삭제
     */
    @DeleteMapping("/reviews/delete/{reviewId}")
    public ResponseEntity<?> deleteReview(@AuthenticationPrincipal CustomUserDetails principal,
                                          @PathVariable Long reviewId) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            myPageService.deleteReview(id, reviewId);
            return ResponseEntity.ok().build();
        } catch (BasicException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus()).build();
        }
    }
}
