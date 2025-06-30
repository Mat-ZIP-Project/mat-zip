package web.mvc.service;

import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.Payment;
import web.mvc.domain.User;
import web.mvc.dto.PaymentCompleteReqDto;
import web.mvc.dto.PaymentCompleteResDto;
import web.mvc.dto.PreparationReqDto;
import web.mvc.dto.PreparationResDto;
import web.mvc.exception.BasicException;

import java.io.IOException;
import java.math.BigDecimal;

public interface PaymentService {

    /**
     *  사전 검증 - 결제창 띄우기 전 호출
     */
    PreparationResDto prepareValid(User user, PreparationReqDto request) throws BasicException, IamportResponseException, IOException;

    /**
     * 결제 완료 처리 - portone 콜백 또는 클라이언트에서 최종적으로 호출
     */
    PaymentCompleteResDto completePayment(PaymentCompleteReqDto request) throws BasicException;

    /**
     * portOne 결제 취소 함수
     */
    Payment cancelPayment(String impUid, BigDecimal amount, String reason) throws BasicException, IamportResponseException, IOException;
}
