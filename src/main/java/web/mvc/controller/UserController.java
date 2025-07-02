package web.mvc.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.mvc.dto.WithdrawUserRequest;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.UserService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdrawUser( @AuthenticationPrincipal CustomUserDetails principal,
                                              @Valid @RequestBody WithdrawUserRequest request) {
        userService.withdrawUser(principal.getUser(), request);
        return ResponseEntity.ok().build();
    }
}
