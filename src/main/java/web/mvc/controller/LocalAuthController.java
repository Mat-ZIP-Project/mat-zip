package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.AuthLogDTO;
import web.mvc.dto.ResAuthDTO;
import web.mvc.dto.ResBadgeDTO;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.LocalAuthService;

import java.util.List;

@RestController
@RequestMapping("/local")
@RequiredArgsConstructor
public class LocalAuthController {

    private final LocalAuthService localAuthService;

    /**
     * 인증하기
     */
    @PostMapping
    public ResponseEntity<?> insertAuthLog(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AuthLogDTO authLogDTO) {
        authLogDTO.setId(userDetails.getUser().getId());
       return ResponseEntity.ok(localAuthService.insertAuthLog(authLogDTO));
    }

    /**
     * 인증기록 검색
     */
    @GetMapping("/auth")
    public ResponseEntity<?> searchAuthLogs(@AuthenticationPrincipal CustomUserDetails userDetails){

        return ResponseEntity.ok(localAuthService.searchAuthLogs(userDetails.getUser().getId()));
    }

    /**
     * 로컬뱃지 정보검색
     */
    @GetMapping("/badges")
    public ResponseEntity<?> searchBadges(@AuthenticationPrincipal CustomUserDetails userDetails){

        return ResponseEntity.ok(localAuthService.searchBadges(userDetails.getUser().getId()));
    }

    /**
     * 인증 삭제
     */
    @DeleteMapping("/badges/{badgeId}")
    public ResponseEntity<?> deleteBages(@PathVariable Long badgeId,@AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(localAuthService.deleteBadges(badgeId,userDetails.getUser().getId()));
    }
}
