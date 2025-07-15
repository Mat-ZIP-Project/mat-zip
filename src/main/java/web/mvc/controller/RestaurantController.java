package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.ResReviewDTO;
import web.mvc.dto.RestaurantListResponseDTO;
import web.mvc.dto.RestaurantDetailDTO;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.RestaurantService;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    /**
     * 식당 목록 조회
     * @param category
     * @param regionSigungu
     * @param sortBy 정렬 기준: likes / rating / reservationCount
     */
    @GetMapping
    public ResponseEntity<List<RestaurantListResponseDTO>> getRestaurantList(
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) String regionSigungu,
            @RequestParam(required = false, defaultValue = "likes") String sortBy
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
    ) {
        List<RestaurantListResponseDTO> list = restaurantService.getRestaurants(category, regionSigungu, sortBy);
        return ResponseEntity.ok(list);
    }

    /**
     * 식당 상세 조회
     * @param restaurantId
     */
    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDetailDTO> getRestaurantDetail(@PathVariable Long restaurantId) {
        RestaurantDetailDTO dto = restaurantService.getRestaurantDetail(restaurantId);
        return ResponseEntity.ok(dto);
    }

    /**
     * 찜 등록
     * @param restaurantId
     */
    @PostMapping("/like/{restaurantId}")
    public ResponseEntity<Void> likeRestaurant(@PathVariable Long restaurantId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        restaurantService.toggleLikeRestaurant(userDetails.getUser().getId(), restaurantId);
        return ResponseEntity.ok().build();
    }

    /**
     * 찜 취소
     * @param restaurantId
     */
    @DeleteMapping("/like/{restaurantId}")
    public ResponseEntity<Void> unlikeRestaurant(@PathVariable Long restaurantId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        restaurantService.toggleLikeRestaurant(userDetails.getUser().getId(), restaurantId);
        return ResponseEntity.ok().build();
    }
    /**
     * 식당 검색
     * @param keyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<RestaurantListResponseDTO>> searchRestaurants(
            @RequestParam String keyword
    ) {
        List<RestaurantListResponseDTO> result = restaurantService.searchRestaurantsByKeyword(keyword);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{restaurantId}/reviews")
    public ResponseEntity<List<ResReviewDTO>> getReviewsByRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam(required = false, defaultValue = "false") boolean localOnly) {

        List<ResReviewDTO> reviews = localOnly
                ? restaurantService.getLocalReviewsByRestaurant(restaurantId)
                : restaurantService.getReviewsByRestaurant(restaurantId);

        return ResponseEntity.ok(reviews);
    }

}
