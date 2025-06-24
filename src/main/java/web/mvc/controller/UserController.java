package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.LoginRequest;
import web.mvc.dto.TokenResponse;
import web.mvc.dto.UserDTO;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.UserService;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
@Slf4j
public class UserController {

    private final ModelMapper modelMapper;
    private final UserService userService;

    @PostMapping("login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(userService.login(req));
    }

    @PostMapping("refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody TokenResponse body) {
        return ResponseEntity.ok(
                userService.refresh(body.getRefreshToken())
        );
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
