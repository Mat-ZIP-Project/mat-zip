package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.mvc.domain.Review;
import web.mvc.dto.*;
import web.mvc.exception.BasicException;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.MenuService;
import web.mvc.service.OwnerService;
import web.mvc.service.ReservationService;
import web.mvc.service.RestaurantReviewService;
import web.mvc.util.WaitingConstants;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/owner")
@Slf4j
public class OwnerController {
    private final OwnerService ownerService;
    private final MenuService menuService;
    private final ReservationService reservationService;

    /**
     * 업주의 식당 정보 조회
     * - 사업자번호, 이미지 목록 포함
     */
    @GetMapping("/restaurant")
    public ResponseEntity<RestaurantInfoResponse> getRestaurantInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUser().getUserId();
        RestaurantInfoResponse response = ownerService.getRestaurantInfo(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 업주의 식당 기본 정보 수정
     * - 주소, 전화번호, 설명, 영업시간, 최대 대기인원 수정
     */
    @PutMapping("/restaurant")
    public ResponseEntity<RestaurantInfoResponse> updateRestaurantInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RestaurantUpdateRequest request) {
        String userId = userDetails.getUser().getUserId();
        RestaurantInfoResponse response = ownerService.updateRestaurantInfo(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 업주의 식당 이미지 목록 조회
     * - 대표이미지 먼저 정렬
     */
    @GetMapping("/restaurant/images")
    public ResponseEntity<List<RestaurantImageResponse>> getRestaurantImages(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUser().getUserId();
        List<RestaurantImageResponse> images = ownerService.getRestaurantImages(userId);
        return ResponseEntity.ok(images);
    }

    /**
     * 업주의 식당 이미지 업로드
     * - 최대 10개 제한
     * - 첫 번째 이미지 자동 대표이미지 설정
     */
    @PostMapping("/restaurant/images")
    public ResponseEntity<List<RestaurantImageResponse>> uploadRestaurantImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("images") List<MultipartFile> images) {
        String userId = userDetails.getUser().getUserId();
        List<RestaurantImageResponse> responses = ownerService.uploadRestaurantImages(userId, images);
        return ResponseEntity.ok(responses);
    }

    /**
     * 업주의 식당 이미지 삭제
     * - 대표이미지 삭제 시 자동 재설정
     */
    @DeleteMapping("/restaurant/images/{imageId}")
    public ResponseEntity<Void> deleteRestaurantImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long imageId) {
        String userId = userDetails.getUser().getUserId();
        ownerService.deleteRestaurantImage(userId, imageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 업주의 식당 대표이미지 설정
     * - 기존 대표이미지 자동 해제
     */
    @PutMapping("/restaurant/images/main/{imageId}")
    public ResponseEntity<Void> setMainImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long imageId) {
        String userId = userDetails.getUser().getUserId();
        ownerService.setMainImage(userId, imageId);
        return ResponseEntity.ok().build();
    }

    ////////////////////////////////////////////////////////////
    // 메뉴 관리 API
    /** 메뉴 목록 조회 */
    @GetMapping("/menu")
    public ResponseEntity<List<MenuResponse>> getMenus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUser().getUserId();
        List<MenuResponse> menus = menuService.getMenus(userId);
        return ResponseEntity.ok(menus);
    }

    /** 메뉴 등록 */
    @PostMapping(value = "/menu", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MenuResponse> createMenu(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("data") MenuRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        System.out.println("Controller 오니 ?");
        System.out.println("MenuRequest" + request.getMenuName() + request.getDescription());
        System.out.println("image : " + image);
        String userId = userDetails.getUser().getUserId();
        MenuResponse created = menuService.createMenu(userId, request, image);
        return ResponseEntity.ok(created);
    }

    /** 메뉴 수정 */
    @PostMapping(value = "/menu/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MenuResponse> updateMenu(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long menuId,
            @RequestPart("data") MenuRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        String userId = userDetails.getUser().getUserId();
        MenuResponse updated = menuService.updateMenu(userId, menuId, request, image);
        return ResponseEntity.ok(updated);
    }

    /** 메뉴 삭제 */
    @DeleteMapping("/menu/{id}")
    public ResponseEntity<Void> deleteMenu(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long menuId) {
        String userId = userDetails.getUser().getUserId();
        menuService.deleteMenu(userId, menuId);
        return ResponseEntity.noContent().build();
    }

    //////////////////////////////////////////////////////
    // 예약 관리
    /** 상태 무관, 최신순 전체 예약 조회 */
    @GetMapping("/reservations/all")
    public ResponseEntity<List<ReservationDetailDto>> getAll(@AuthenticationPrincipal CustomUserDetails principal) throws BasicException {
        List<ReservationDetailDto> list = ownerService.getAllReservations(principal.getUsername());
        return ResponseEntity.ok(list);
    }

    /** 오늘 날짜 예약 조회 (시간순) */
    @GetMapping("/reservation/today")
    public ResponseEntity<List<ReservationDetailDto>> getToday(@AuthenticationPrincipal CustomUserDetails principal) throws BasicException {
        List<ReservationDetailDto> list = ownerService.getTodayReservations(principal.getUsername());
        return ResponseEntity.ok(list);
    }

    /** 대기 중 예약 목록 조회 */
    @GetMapping("/reservations/pending")
    public ResponseEntity<List<PendingReservationDto>> getPending(@AuthenticationPrincipal CustomUserDetails principal) throws BasicException {
        return ResponseEntity.ok(reservationService.getPendingReservations(principal.getUsername()));
    }

    /** 노쇼 후보 목록 조회 */
    @GetMapping("/reservations/noshow")
    public ResponseEntity<List<NoShowReservationDto>> getNoShowList(@AuthenticationPrincipal CustomUserDetails principal) throws BasicException {
        return ResponseEntity.ok(reservationService.getNoShowCandidates(principal.getUsername()));
    }

    /** 노쇼 처리 */
    @PostMapping("/reservations/noshow")
    public ResponseEntity<String> markNoShow(@RequestBody NoShowRequest request) throws BasicException {
        reservationService.markNoShow(request.getReservationId());
        return ResponseEntity.ok("노쇼 처리 완료");
    }
    //////////////////////////////////////////////////////////
    // 웨이팅 관리
    /** 웨이팅 '입장 대기' 명단 조회 */
    @GetMapping("/waiting/list")
    public ResponseEntity<List<WaitingListResponse>> getWaitingList(@AuthenticationPrincipal CustomUserDetails principal) throws BasicException {
        List<WaitingListResponse> responses = ownerService.getWaitingListByRestaurantAndStatus(principal.getUsername(), WaitingConstants.STATUS_WAITING);
        System.out.println("웨이팅입장대기 "+responses.size());
        return ResponseEntity.ok(responses);
    }
    /** 웨이팅 '호출' 명단 조회 */
    @GetMapping("/waiting/call-list")
    public ResponseEntity<List<WaitingListResponse>> getCallList(@AuthenticationPrincipal CustomUserDetails principal) throws BasicException {
        List<WaitingListResponse> responses = ownerService.getWaitingListByRestaurantAndStatus(principal.getUsername(), WaitingConstants.STATUS_CALLED);
        System.out.println("웨이팅호출명단 "+responses.size());
        return ResponseEntity.ok(responses);
    }

    //////////////////////////////////////////////////////////
    // 리뷰 관리
    @GetMapping("/reviews/all")
    public ResponseEntity<List<ReviewDetailResponse>> getMyReviews(@AuthenticationPrincipal CustomUserDetails principal) {
        List<ReviewDetailResponse> responses = ownerService.getReviewsByUserId(principal.getUsername());
        return ResponseEntity.ok(responses);
    }
}

