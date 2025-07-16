package web.mvc.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import web.mvc.dto.WaitingRegisterRequestDTO;
import web.mvc.dto.WaitingRegisterResponseDTO;
import web.mvc.dto.WaitingStatusResponseDTO;
import web.mvc.security.CustomUserDetails;
import web.mvc.security.JwtTokenProvider;
import web.mvc.service.WaitingService;
import web.mvc.util.SseEmitterManager;

import java.util.List;

@RestController
@RequestMapping("/api/waiting")
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;
    private final SseEmitterManager sseEmitterManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * [POST] 웨이팅 등록
     * - 로그인한 사용자가 특정 식당에 웨이팅 등록 요청
     * - 요청 바디에는 식당 ID, 인원 수 등이 포함됨
     * - 웨이팅 번호, 예상 입장 시간 등을 응답으로 반환
     */
    @PostMapping
    public ResponseEntity<WaitingRegisterResponseDTO> registerWaiting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WaitingRegisterRequestDTO requestDto) {

        String userId = userDetails.getUsername();
        WaitingRegisterResponseDTO response = waitingService.registerWaitingByUserId(userId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * [GET] 나의 웨이팅 상태 조회
     * - 로그인한 사용자의 현재 웨이팅 상태(순번, 식당명, 예상 입장 시간 등)를 반환
     */
    @GetMapping("/me")
    public ResponseEntity<WaitingStatusResponseDTO> getMyWaitingStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getUsername();
        WaitingStatusResponseDTO response = waitingService.getMyWaitingStatus(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * [PUT] 다음 대기자 호출
     * - 식당 주인이 호출 시 가장 먼저 등록된 웨이팅 유저를 호출 상태로 변경
     * - 호출된 유저에게 SSE 알림 발송
     */
    @PutMapping("/next/{restaurantId}")
    public ResponseEntity<Void> callNextWaiting(@PathVariable Long restaurantId) {
        waitingService.callNextWaiting(restaurantId);
        return ResponseEntity.ok().build();
    }

    /**
     * [GET] 특정 식당의 웨이팅 현황 조회
     * - 현재 대기 인원 수, 예상 입장 시간 등을 반환
     */
    @GetMapping("/status/{restaurantId}")
    public ResponseEntity<WaitingStatusResponseDTO> getWaitingStatusByRestaurant(@PathVariable Long restaurantId) {
        WaitingStatusResponseDTO response = waitingService.getWaitingStatusByRestaurantId(restaurantId);
        System.out.println("웨이팅현황:" +response);
        return ResponseEntity.ok(response);
    }

    /**
     * [PUT] 호출된 웨이팅 유저 입장 처리
     * - 상태가 '호출됨'인 대기자만 입장 가능
     * - 입장 시 상태를 '입장 완료'로 변경
     */
    @PutMapping("/enter/{waitingId}")
    public ResponseEntity<Void> enterWaiting(@PathVariable Long waitingId) {
        waitingService.enterWaitingUser(waitingId);
        return ResponseEntity.ok().build();
    }

    /**
     * [GET] 본인이 소유한 모든 식당의 웨이팅 현황 조회
     * - 식당 주인 로그인 상태에서 호출됨
     */
    @GetMapping("/owner/me")
    public ResponseEntity<List<WaitingStatusResponseDTO>> getMyRestaurantsWaitingStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String ownerId = userDetails.getUsername();
        List<WaitingStatusResponseDTO> response = waitingService.getWaitingStatusesByOwnerId(ownerId);
        return ResponseEntity.ok(response);
    }

    /**
     * [PUT] 식당 주인이 특정 웨이팅 유저를 노쇼 처리 시도
     * - 호출된 후 15분이 지나지 않았다면 예외 발생
     * - 15분 이상 지난 경우에만 노쇼 처리 가능
     */
    @PutMapping("/noshow/{waitingId}")
    public ResponseEntity<Void> markNoShow(@PathVariable Long waitingId) {
        waitingService.markNoShow(waitingId);
        return ResponseEntity.ok().build();
    }

    /**
     * [GET] SSE 구독 요청
     * - 사용자가 이 API를 호출하면 서버와의 SSE 연결을 시작
     * - 이후 서버에서 이벤트가 발생할 때마다 실시간으로 전달됨
     */
    @CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam String token) {
        String userId = jwtTokenProvider.getUserId(token);
        return sseEmitterManager.createEmitter(userId);
    }

}
