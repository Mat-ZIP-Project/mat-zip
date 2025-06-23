package web.mvc.service;

import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PortOneServiceImpl implements PortOneService { // PortOneService 인터페이스 구현

    private final WebClient webClient;

    @Value("${portone.api.key}")
    private String portOneApiKey;

    @Value("${portone.api.secret}")
    private String portOneApiSecret;

    // Access Token을 캐싱하기 위한 필드 (실제 환경에서는 Redis 등으로 관리 고려)
    private String cachedAccessToken;
    private long accessTokenExpiryTime; // Epoch seconds

    @Autowired // 생성자 주입
    public PortOneServiceImpl(WebClient.Builder webClientBuilder, @Value("${portone.api.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public synchronized String getAccessToken() {
        long currentTime = System.currentTimeMillis() / 1000; // 현재 시간 (초 단위)

        // 토큰이 유효하면 캐싱된 토큰 반환 (만료 시간 60초 전에 미리 갱신 시도)
        if (cachedAccessToken != null && (accessTokenExpiryTime - 60) > currentTime) {
            return cachedAccessToken;
        }

        // 토큰 발급 요청 바디
        Map<String, String> tokenRequest = new HashMap<>();
        tokenRequest.put("imp_key", portOneApiKey);
        tokenRequest.put("imp_secret", portOneApiSecret);

        // PortOne API 호출하여 access_token 발급
        PortOneTokenResponse response = webClient.post()
                .uri("/users/getToken")
                .bodyValue(tokenRequest)
                .retrieve()
                .bodyToMono(PortOneTokenResponse.class)
                .block(); // 블로킹 호출 (실제 환경에서는 비동기 처리 고려)

        if (response != null && response.getResponse() != null) {
            cachedAccessToken = response.getResponse().getAccess_token();
            accessTokenExpiryTime = response.getResponse().getExpired_at(); // PortOne의 expired_at은 Unix Timestamp (seconds)
            System.out.println("PortOne Access Token 발급 성공: " + cachedAccessToken + ", 만료: " + accessTokenExpiryTime);
            return cachedAccessToken;
        } else {
            System.err.println("PortOne Access Token 발급 실패.");
            throw new RuntimeException("Failed to get PortOne Access Token."); // 토큰 발급 실패 시 예외 발생
        }
    }

    @Override
    public boolean verifyPayment(String impUid, Integer expectedAmount) {
        String accessToken = getAccessToken(); // Access Token 획득
        if (accessToken == null || accessToken.isEmpty()) {
            System.err.println("결제 검증 실패: Access Token 없음.");
            return false;
        }

        // PortOne API 호출하여 결제 정보 조회
        PortOnePaymentResponse paymentResponse = webClient.get()
                .uri("/payments/{imp_uid}", impUid)
                .header("Authorization", "Bearer " + accessToken) // Access Token을 헤더에 포함
                .retrieve()
                .bodyToMono(PortOnePaymentResponse.class)
                .block();

        if (paymentResponse != null && paymentResponse.getResponse() != null) {
            PortOnePaymentResponse.PortOnePaymentResponseData paymentData = paymentResponse.getResponse();
            // 결제 상태가 'paid'이고, 실제 결제 금액과 예상 금액이 일치하는지 검증
            if ("paid".equals(paymentData.getStatus()) && expectedAmount.equals(paymentData.getAmount())) {
                System.out.println("PortOneService: 결제 검증 성공 - imp_uid: " + impUid + ", 실제 금액: " + paymentData.getAmount());
                return true;
            } else {
                System.err.println("PortOneService: 결제 검증 실패 - 상태 불일치 또는 금액 불일치. Status: " + paymentData.getStatus() + ", Amount: " + paymentData.getAmount() + ", Expected: " + expectedAmount);
                return false;
            }
        } else {
            System.err.println("PortOneService: 결제 정보 조회 실패 - imp_uid: " + impUid + ". PortOne 응답 없음.");
            return false;
        }
    }

    @Override
    public boolean refundPayment(String impUid, Integer amount, String reason) {
        String accessToken = getAccessToken(); // Access Token 획득
        if (accessToken == null || accessToken.isEmpty()) {
            System.err.println("결제 환불 실패: Access Token 없음.");
            return false;
        }

        // 환불 요청 바디
        Map<String, Object> refundRequest = new HashMap<>();
        refundRequest.put("imp_uid", impUid);
        refundRequest.put("amount", amount);
        refundRequest.put("reason", reason);

        // PortOne API 호출하여 환불 요청
        return webClient.post()
                .uri("/payments/cancel")
                .header("Authorization", "Bearer " + accessToken) // Access Token을 헤더에 포함
                .bodyValue(refundRequest)
                .retrieve()
                .toBodilessEntity() // 응답 본문이 필요 없을 때
                .map(response -> {
                    // HTTP 상태 코드가 2xx (성공) 범위인지 확인
                    boolean success = response.getStatusCode().is2xxSuccessful();
                    System.out.println("PortOneService: 결제 환불 요청 결과 HTTP Status: " + response.getStatusCode() + ", 성공 여부: " + success);
                    return success;
                })
                .block();
    }
}
