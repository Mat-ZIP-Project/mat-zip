package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.mvc.dto.SendSmsRequest;
import web.mvc.dto.VerifySmsRequest;
import web.mvc.service.SmsVerificationService;

@RestController
@RequestMapping("/signup")
@RequiredArgsConstructor
public class SignupController {

    private final SmsVerificationService smsService;

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
}
