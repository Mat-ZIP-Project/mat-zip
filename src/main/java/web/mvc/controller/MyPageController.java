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

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Slf4j
public class MyPageController {

    private final MyPageService myPageService;

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
     *  사용자가 참여한 모임 내역을 조회
     */
    @GetMapping("/meetings/participants")
    public ResponseEntity<List<ParticipantDetailDto>> getUserMeetings(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            List<ParticipantDetailDto> meetupParticipant = myPageService.getParticipatedMeetings(id);
            return  ResponseEntity.ok(meetupParticipant);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus()).build();
        }
    }

    /**
     *  사용자가 모임에 대해 작성한 리뷰 내역
     */
    @GetMapping("/meetings/reviews")
    public ResponseEntity<List<MeetingReviewDetailDto>>  getUserMeetingReviews(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            List<MeetingReviewDetailDto> meetingReviews = myPageService.getMeetingReviews(id);
            return ResponseEntity.ok(meetingReviews);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus()).build();
        }
    }

    /**
     *  사용자가 직접 생성한 모임 내역
     */
    @GetMapping("/meetings/created")
    public ResponseEntity<List<CreatedMeetingDetailDto>> getUserMeetingCreated(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            List<CreatedMeetingDetailDto> meetings = myPageService.getMeeting(id);
            return ResponseEntity.ok(meetings);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus()).build();
        }
    }

    /**
     * 사용자 예약 취소
     */
    @PostMapping("/reservations/{reservationId}/cancel")
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
    public ResponseEntity<List<Notification>> getUserNotifications(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            List<Notification> notificationList = myPageService.getUserNotification(id);
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

}
