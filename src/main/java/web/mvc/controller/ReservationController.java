package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.mvc.dto.ReservationCreateReqDto;
import web.mvc.dto.ReservationCreateResDto;
import web.mvc.exception.BasicException;
import web.mvc.service.ReservationService;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/create")
    public ResponseEntity<ReservationCreateResDto> createReservation(@RequestBody ReservationCreateReqDto request) {
        log.info("예약 생성 요청 수신: 날짜={}, 시간={}, 인원={}, 식당이름={}, userId={}",
                request.getDate(), request.getTime(), request.getNumPeople(),
                request.getRestaurantName(), request.getId());
        try {
            ReservationCreateResDto response = reservationService.createReservation(request);
            // 성공 시 200 OK와 응답 DTO 반환
            return ResponseEntity.ok(response);
        } catch (BasicException e) {
            log.error("예약 생성 실패: {}", e.getMessage());
            // BasicException의 ErrorCode에 따라 적절한 HTTP 상태 코드와 메시지 반환
            ReservationCreateResDto errorResponse = new ReservationCreateResDto(null, e.getMessage(), false);
            return ResponseEntity.status(e.getErrorCode().getStatus()).body(errorResponse);
        } catch (Exception e) {
            log.error("알 수 없는 예약 생성 오류 발생: {}", e.getMessage(), e);
            ReservationCreateResDto errorResponse = new ReservationCreateResDto(null, "서버 오류로 예약 생성에 실패했습니다.", false);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
