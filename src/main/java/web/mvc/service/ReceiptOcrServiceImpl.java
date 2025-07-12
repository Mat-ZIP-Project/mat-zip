package web.mvc.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import web.mvc.domain.QReview;
import web.mvc.domain.Restaurant;
import web.mvc.domain.Review;
import web.mvc.dto.*;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.RestaurantRepository;


import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReceiptOcrServiceImpl implements ReceiptOcrService {
    private final RestTemplate restTemplate;
    private final RestaurantRepository restaurantRepository;
    private final JPAQueryFactory jpaQueryFactory;
    @Value("${naver.ocr.api-url}")
    private String apiUrl;

    @Value("${naver.ocr.client-id}")
    private String clientId;

    @Value("${naver.ocr.client-secret}")
    private String clientSecret;

    @Override
    public ResOcrDTO parseReceipt(MultipartFile image,Long restaurantId,Long id) {
        try {


            ObjectMapper mapper = new ObjectMapper();
            // 1. 이미지 파일을 Base64로 인코딩
            String base64Image = Base64.getEncoder().encodeToString(image.getBytes());

            OcrImage imageObj = new OcrImage("jpg", "receipt", base64Image);
            List<OcrImage> images = Collections.singletonList(imageObj);
            // 2. JSON 요청 바디 구성
            OcrRequest body = new OcrRequest(
                    "V2",
                    UUID.randomUUID().toString(),
                    System.currentTimeMillis(),
                    images
            );

            // Jackson으로 JSON 문자열 생성
            String jsonBody = mapper.writeValueAsString(body);

            // 3. Http 요청 전송
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-OCR-SECRET", clientSecret);

            HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            // 4. 결과 파싱
            return extractRestaurantInfo(response.getBody(),restaurantId,id);

        } catch (Exception e) {
            throw new RuntimeException("OCR 처리 실패", e);
        }
    }

    private void validateReceipt(Long restaurantId,String restaurantName,String visitDate,Long id){
        Restaurant restaurant=restaurantRepository.findById(restaurantId).get();
        if(!restaurant.getRestaurantName().equals(restaurantName)){throw new BasicException(ErrorCode.RESTAURANT_MISMATCH);}

        LocalDate parsedVisitDate = LocalDate.parse(visitDate);
        LocalDate now = LocalDate.now();

        // 7일 이내인지 검사
        if (parsedVisitDate.isBefore(now.minusDays(7))) {
            throw new BasicException(ErrorCode.INVALID_DATE);
        }

        QReview review=QReview.review;
        List<Review> res = jpaQueryFactory.selectFrom(review)
                .where(review.user.id.eq(id).and(review.restaurant.restaurantId.eq(restaurantId)).and(review.visitDate.eq(parsedVisitDate))).fetch();
        if(res.size()!=0) throw new BasicException(ErrorCode.DUPLICATED_REVIEW);

    }




    private ResOcrDTO extractRestaurantInfo(String responseJson,Long restaurantId,Long id) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            OcrResponse response = mapper.readValue(responseJson, OcrResponse.class);

            List<OcrField> fields = response.getImages().get(0).getFields();

            String restaurantName = null;
            String visitDate = null;

            for (OcrField field : fields) {
                String text = field.getInferText();
                if (restaurantName == null && text.matches("^[가-힣a-zA-Z0-9\\s]+$")) {
                    restaurantName = text;
                }
                if (visitDate == null && text.matches("\\d{4}[-./]\\d{2}[-./]\\d{2}")) {
                    visitDate = text.replaceAll("[^0-9]", "-");
                }
            }

            validateReceipt(restaurantId,restaurantName,visitDate,id);
            return new ResOcrDTO(restaurantName, visitDate);
        } catch (Exception e) {
            throw new RuntimeException("OCR 결과 파싱 실패", e);
        }
    }
}


