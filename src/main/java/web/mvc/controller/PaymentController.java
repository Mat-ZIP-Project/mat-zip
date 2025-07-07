package web.mvc.controller;

import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.mvc.domain.User;
import web.mvc.dto.*;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.PaymentService;

import java.io.IOException;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     *  결제 전 사전 검증을 위해 호출
     *  사용자 검증 필요
     */
    @PostMapping("/prepare")
    public ResponseEntity<PreparationResDto> prepareValid(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody PreparationReqDto request) throws IamportResponseException, IOException {

        User user = principal.getUser();
        log.info("사전 검증 요청", request.toString());
        PreparationResDto response = paymentService.prepareValid(user, request);
//        PreparationResDto response = paymentService.prepareValid(request);
        return ResponseEntity.ok(response);
    }

    /**
     *  결제 완료 후 최종 검증 및 처리를 위해 호출
     *  사용자 검증 불필요
     */
    @PostMapping("/complete")
    public ResponseEntity<PaymentCompleteResDto> completePayment(@RequestBody PaymentCompleteReqDto request) throws IamportResponseException, IOException {
        log.info("결제 완료 요청", request.toString());
        PaymentCompleteResDto response = paymentService.completePayment(request);
        return ResponseEntity.ok(response);
    }
}
