package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import web.mvc.domain.Reservation;
import web.mvc.domain.Review;
import web.mvc.dto.ReservationDetailDto;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.MyPageService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
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
     *  사용자의 전체 모임 내역 조회
     */
    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getUserReviews(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = principal.getUser().getId();

        try {
            List<Review> reviews = myPageService.getUserReviews(id);
            return ResponseEntity.ok(reviews);
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

}
