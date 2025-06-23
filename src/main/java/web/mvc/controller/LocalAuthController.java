package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.AuthLogDTO;
import web.mvc.dto.ResAuthDTO;
import web.mvc.dto.ResBadgeDTO;
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
    public String insertAuthLog(@RequestBody AuthLogDTO authLogDTO) {


       return localAuthService.insertAuthLog(authLogDTO);
    }

    /**
     * 인증기록 검색
     */
    @GetMapping("/auth")
    public List<ResAuthDTO> searchAuthLogs(/*@AuthenticationPrincipal*/ @RequestParam Long id){

        return localAuthService.searchAuthLogs(id);
    }

    /**
     * 로컬뱃지 정보검색
     */
    @GetMapping("/badges")
    public List<ResBadgeDTO> searchBadges(/*@AuthenticationPrincipal*/@RequestParam Long id){

        return localAuthService.searchBadges(id);
    }

    /**
     * 인증 삭제
     */

}
