package web.mvc.controller;

import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.mvc.dto.*;
import web.mvc.service.PaymentService;

import java.io.IOException;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/prepare")
    public Response<PreparationResDto> prepareValid(@RequestBody PreparationReqDto request) throws IamportResponseException, IOException {
        log.info("사전 검증 요청", request.toString());
        PreparationResDto response = paymentService.prepareValid(request);
        return Response.success(response);
    }

    @PostMapping("/complete")
    public Response<PaymentCompleteResDto> completePayment(@RequestBody PaymentCompleteReqDto request) throws IamportResponseException, IOException {
        log.info("결제 완료 요청", request.toString());
        PaymentCompleteResDto response = paymentService.completePayment(request);
        return Response.success(response);
    }
}
