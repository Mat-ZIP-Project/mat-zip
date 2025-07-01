package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import web.mvc.domain.Restaurant;
import web.mvc.domain.User;
import web.mvc.domain.WaitingQueue;
import web.mvc.dto.CommonResponseDto;
import web.mvc.dto.WaitingListResponseDto;
import web.mvc.dto.WaitingMyPageResponseDto;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.RestaurantRepository;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.WaitingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/waiting")
public class WaitingController {

    private final WaitingService waitingService;
    private final RestaurantRepository restaurantRepository;

    @PostMapping("/{restaurantId}")
    public ResponseEntity<CommonResponseDto> registerWaiting(@PathVariable Long restaurantId,
                                                             @RequestParam int numPeople,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        waitingService.registerWaiting(user, restaurant, numPeople);

        return ResponseEntity.ok(new CommonResponseDto("정상적으로 웨이팅이 등록되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyWaiting(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        WaitingQueue waiting = waitingService.getMyActiveWaiting(user);

        return ResponseEntity.ok(WaitingMyPageResponseDto.from(waiting));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<?> getRestaurantWaitingList(@PathVariable Long restaurantId,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        // 로그인한 사장님과 이 식당이 연결된 식당인지 확인
        if (!restaurant.getOwner().getUser().getId().equals(userDetails.getUser().getId())) {
            throw new BasicException(ErrorCode.RESTAURANT_NOT_FOUND);
        }

        List<WaitingQueue> waitings = waitingService.getWaitingListByRestaurant(restaurant);
        List<WaitingListResponseDto> result = waitings.stream()
                .map(WaitingListResponseDto::from)
                .toList();

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{waitingId}/status")
    public ResponseEntity<?> updateWaitingStatus(@PathVariable Long waitingId,
                                                 @RequestParam String value,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        WaitingQueue waiting = waitingService.getWaitingById(waitingId);

        // 권한 확인: 사장님 본인 식당의 웨이팅인지 체크
        if (!waiting.getRestaurant().getOwner().getUser().getId()
                .equals(userDetails.getUser().getId())) {
            throw new BasicException(ErrorCode.FORBIDDEN);
        }

        // 상태 값 유효성 검사
        if (!value.equals("입장 완료") && !value.equals("노쇼")) {
            throw new BasicException(ErrorCode.INVALID_INPUT);
        }

        waitingService.updateWaitingStatus(waitingId, value);
        return ResponseEntity.ok(new CommonResponseDto("웨이팅 상태가 [" + value + "]로 변경되었습니다."));
    }





}

