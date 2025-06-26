package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.LoginRequest;
import web.mvc.dto.TokenResponse;
import web.mvc.dto.UserDTO;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.TokenService;
import web.mvc.service.UserService;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
@Slf4j
public class UserController {

    private final ModelMapper modelMapper;
    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody TokenResponse body) {
        return ResponseEntity.ok(
                tokenService.refreshTokens(body.getRefreshToken())
        );
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails principal) {
        userService.logout(principal.getUser());
        return ResponseEntity.ok().build();
    }

    /** 로그인된 사용자 정보 조회 */
    @GetMapping("user")
    public ResponseEntity<UserDTO> getUser(@AuthenticationPrincipal CustomUserDetails principal) {
        //Modelmapper로 매핑
        UserDTO user = modelMapper.map(principal.getUser(), UserDTO.class);

        log.info("로그인 사용자 DTO = {}", user);
        return ResponseEntity.ok(user);
    }

}
