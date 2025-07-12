package web.mvc.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.WaitingRegisterRequestDTO;
import web.mvc.dto.WaitingRegisterResponseDTO;
import web.mvc.dto.WaitingStatusResponseDTO;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.WaitingService;

@RestController
@RequestMapping("/api/waiting")
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;

    /**
     * 웨이팅 등록
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
     * 내 웨이팅 상태 조회
     */
    @GetMapping("/me")
    public ResponseEntity<WaitingStatusResponseDTO> getMyWaitingStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUsername();
        WaitingStatusResponseDTO response = waitingService.getMyWaitingStatus(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 다음 손님 입장 처리 (관리자 또는 식당 주인이 호출)
     */
    @PutMapping("/next/{restaurantId}")
    public ResponseEntity<Void> callNextWaiting(@PathVariable Long restaurantId) {
        waitingService.callNextWaiting(restaurantId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{restaurantId}")
    public ResponseEntity<WaitingStatusResponseDTO> getWaitingStatusByRestaurant(@PathVariable Long restaurantId) {
        WaitingStatusResponseDTO response = waitingService.getWaitingStatusByRestaurantId(restaurantId);
        return ResponseEntity.ok(response);
    }
}
