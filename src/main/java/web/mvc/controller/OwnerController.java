package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.mvc.dto.MenuRequest;
import web.mvc.dto.MenuResponse;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.MenuService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/owner")
@Slf4j
public class OwnerController {
    private final MenuService menuService;

    /** 메뉴 목록 조회 */
    @GetMapping("/menu")
    public ResponseEntity<List<MenuResponse>> getMenus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUser().getUserId();
        List<MenuResponse> menus = menuService.getMenus(userId);
        return ResponseEntity.ok(menus);
    }

    /** 메뉴 등록 */
    @PostMapping(value = "/menu")
    public ResponseEntity<MenuResponse> createMenu(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("data") MenuRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        String userId = userDetails.getUser().getUserId();
        MenuResponse created = menuService.createMenu(userId, request, image);
        return ResponseEntity.ok(created);
    }

    /** 메뉴 수정 */
    @PutMapping(value = "/menu/{id}")
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
}

