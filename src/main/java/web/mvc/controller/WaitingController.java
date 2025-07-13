package web.mvc.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import web.mvc.dto.WaitingRegisterRequestDTO;
import web.mvc.dto.WaitingRegisterResponseDTO;
import web.mvc.dto.WaitingStatusResponseDTO;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.WaitingService;
import web.mvc.util.SseEmitterManager;


import java.util.List;

@RestController
@RequestMapping("/api/waiting")
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;
    private final SseEmitterManager sseEmitterManager;

    /**
     * 웨이팅 등록
     * - 로그인한 사용자가 요청
     * - 사용자 ID는 시큐리티 세션에서 가져옴
     * - 요청 바디로는 웨이팅할 식당 ID, 인원 수 등을 포함
     */
    @PostMapping
    public ResponseEntity<WaitingRegisterResponseDTO> registerWaiting(
            @AuthenticationPrincipal CustomUserDetails userDetails, // 로그인된 사용자 정보 주입
            @Valid @RequestBody WaitingRegisterRequestDTO requestDto) { // 요청 바디 유효성 검증

        String userId = userDetails.getUsername(); // 사용자 ID 추출
        WaitingRegisterResponseDTO response = waitingService.registerWaitingByUserId(userId, requestDto); // 서비스 호출

        return ResponseEntity.ok(response); // 응답 반환
    }

    /**
     * 나의 웨이팅 상태 조회
     * - 현재 로그인한 사용자의 웨이팅 상태 확인
     * - 대기 중인 식당, 순번, 예상 입장 시간 등을 포함
     */
    @GetMapping("/me")
    public ResponseEntity<WaitingStatusResponseDTO> getMyWaitingStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getUsername(); // 사용자 ID 추출
        WaitingStatusResponseDTO response = waitingService.getMyWaitingStatus(userId); // 웨이팅 정보 조회
        return ResponseEntity.ok(response); // 응답 반환
    }

    /**
     * 다음 대기자 호출
     * - 특정 식당에서 다음 손님을 호출하는 API
     * - 관리자 또는 식당 주인 권한이 필요할 수 있음 (보안 로직은 Security 설정에 따라)
     */
    @PutMapping("/next/{restaurantId}")
    public ResponseEntity<Void> callNextWaiting(@PathVariable Long restaurantId) {
        waitingService.callNextWaiting(restaurantId); // 다음 웨이팅 상태 변경 처리
        return ResponseEntity.ok().build(); // 바디 없이 성공 응답 반환
    }

    /**
     * 특정 식당의 웨이팅 현황 조회
     * - 현재 대기 인원 수, 예상 입장 시간 등을 확인 가능
     */
    @GetMapping("/status/{restaurantId}")
    public ResponseEntity<WaitingStatusResponseDTO> getWaitingStatusByRestaurant(@PathVariable Long restaurantId) {
        WaitingStatusResponseDTO response = waitingService.getWaitingStatusByRestaurantId(restaurantId);
        return ResponseEntity.ok(response);
    }

    /**
     * 호출된 웨이팅 유저의 입장 처리
     * - 호출 상태(CALLED)인 유저가 실제로 입장했을 때 호출됨
     * - waitingId로 해당 대기 정보를 찾아 상태를 'ENTERED'로 변경
     * - 일반적으로 키오스크 또는 식당 직원 화면에서 입장 버튼을 눌렀을 때 호출되는 API
     */
    @PutMapping("/enter/{waitingId}")
    public ResponseEntity<Void> enterWaiting(@PathVariable Long waitingId) {
        waitingService.enterWaitingUser(waitingId);
        return ResponseEntity.ok().build();
    }

    /**
     * [식당 주인 전용] 본인이 소유한 식당의 웨이팅 현황 리스트 조회
     */
    @GetMapping("/owner/me")
    public ResponseEntity<List<WaitingStatusResponseDTO>> getMyRestaurantsWaitingStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String ownerId = userDetails.getUsername(); // 로그인한 식당 주인의 사용자 ID
        List<WaitingStatusResponseDTO> response = waitingService.getWaitingStatusesByOwnerId(ownerId);

        return ResponseEntity.ok(response);
    }

    /**
     * 식당 주인이 특정 대기자를 노쇼 처리 시도
     */
    @PutMapping("/noshow/{waitingId}")
    public ResponseEntity<Void> markNoShow(@PathVariable Long waitingId) {
        waitingService.markNoShow(waitingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUsername();
        return sseEmitterManager.createEmitter(userId);
    }


}
