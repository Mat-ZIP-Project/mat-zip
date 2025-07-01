package web.mvc.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.*;
import web.mvc.service.SignupService;
import web.mvc.service.SmsVerificationService;
import web.mvc.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/signup")
@RequiredArgsConstructor
public class SignupController {

    private final SmsVerificationService smsService;
    private final SignupService signupService;
    private final UserService userService;

    /** 아이디 중복체크 */
    @PostMapping("/check/userid")
    public ResponseEntity<Void> checkUserIdDuplicate(@RequestBody SignupDuplicateRequest request) {
        signupService.checkUserIdDuplicate(request.getUserId());
        return ResponseEntity.ok().build();
    }

    /** 휴대폰번호 중복체크 */
    @PostMapping("/check/phone")
    public ResponseEntity<Void> checkPhoneDuplicate(@RequestBody SignupDuplicateRequest  request) {
        signupService.checkPhoneDuplicate(request.getPhone());
        return ResponseEntity.ok().build();
    }

    /** 사업자등록번호 유효성 검증 */
    @PostMapping("/verify/business")
    public ResponseEntity<Void> verifyBusinessNumber(@RequestBody SignupDuplicateRequest request) {
        signupService.verifyBusinessNumber(request.getBusinessNumber());
        return ResponseEntity.ok().build();
    }

    /** SMS 인증코드 발송 */
    @PostMapping("/sms/send")
    public ResponseEntity<Void> sendCode(@RequestBody SendSmsRequest req) {
        smsService.sendVerificationCode(req.getPhone(), req.getPurpose());
        return ResponseEntity.ok().build();
    }

    /** SMS 인증코드 검증 */
    @PostMapping("/sms/verify")
    public ResponseEntity<Void> verifyCode(@RequestBody VerifySmsRequest req) {
        smsService.verifyCode(req.getPhone(), req.getCode(), req.getPurpose());
        return ResponseEntity.ok().build();
    }

    /** 식당 주소 검색 (식당 사장용) */
    @GetMapping("/address")
    public ResponseEntity<List<AddressResponse>> searchAddress(@RequestParam("query") String query) {
        List<AddressResponse> list = signupService.searchAddress(query);
        return ResponseEntity.ok(list);
    }

    /** 일반 사용자 회원가입 */
    @PostMapping("/user")
    public ResponseEntity<Void> signupUser(@Valid @RequestBody SignupRequest request) {
        request.setRole("ROLE_USER");
        signupService.signupUser(request);
        return ResponseEntity.ok().build();
    }

    /** 식당주 회원가입 */
    @PostMapping("/owner")
    public ResponseEntity<Void> signupOwner(@Valid @RequestBody SignupOwnerRequest request) {
        request.setRole("ROLE_OWNER");
        signupService.signupOwner(request);
        return ResponseEntity.ok().build();
    }
}
