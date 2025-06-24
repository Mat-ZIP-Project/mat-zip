//package web.mvc.service;
//
//import com.siot.IamportRestClient.IamportClient;
//import com.siot.IamportRestClient.response.IamportResponse;
//import com.siot.IamportRestClient.response.Payment;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import web.mvc.repository.PaymentRepository;
//
//@Service
//public class PortOneServiceImpl implements PortOneService { // PortOneService 인터페이스 구현
//
//    private final IamportClient client;
//    private final PaymentRepository paymentRepository;
//
//    @Value("${iamport.rest-api-key}")
//    private String portOneApiKey;
//
//    @Value("${iamport.rest-api-secret}")
//    private String portOneApiSecret;
//
//    @Autowired // 생성자 주입
//    public PortOneServiceImpl(PaymentRepository paymentRepository) {
//        this.client = new IamportClient(portOneApiKey, portOneApiSecret);
//        this.paymentRepository = paymentRepository;
//    }
//
//
//    @Override
//    public PortOneWebhookDto veifyPayment(String impUid) throws Exception {
//        IamportResponse<Payment> iamportResponse = client.paymentByImpUid(impUid);
//
//
//        return null;
//    }
//}
