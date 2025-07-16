package web.mvc.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptOcrServiceImpl implements ReceiptOcrService {
    private final RestTemplate restTemplate;
    private final RestaurantRepository restaurantRepository;
    private final JPAQueryFactory jpaQueryFactory;
    @Value("${naver.ocr.api-url}")
    private String apiUrl;

//    @Value("${naver.ocr.client-id}")
//    private String clientId;

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

            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            log.info(response.getBody());

            // 4. 결과 파싱
            return extractRestaurantInfo(response.getBody(), restaurantId, id);

        } catch (BasicException e) {
            log.warn("OCR 도메인 예외 발생: {}", e.getMessage(), e);
            throw e; // 도메인 검증 실패는 그대로 던짐
        } catch (IOException e) {
            log.error("OCR 이미지 인코딩 또는 JSON 직렬화 실패", e);
            throw new RuntimeException("영수증 이미지를 처리하는 도중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("OCR 처리 중 알 수 없는 예외 발생", e);
            throw new RuntimeException("영수증 인증 중 시스템 오류가 발생했습니다.", e);
        }
    }

    private void validateReceipt(Long restaurantId,String restaurantName,String visitDate,Long id){
        Restaurant restaurant=restaurantRepository.findById(restaurantId).get();

        String extracted = restaurantName.replaceAll("\\s+", ""); // OCR 결과 공백 제거
        String target = restaurant.getRestaurantName().replaceAll("\\s+", ""); // DB 값 공백 제거

        log.info("restaurantName={},visitDate={}",restaurantName,visitDate);
        log.info("restaurantId={},foundrestaurantName={}",restaurantId,restaurant.getRestaurantName());
        if(!extracted.equals(target)){throw new BasicException(ErrorCode.RESTAURANT_MISMATCH);}

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




    private ResOcrDTO extractRestaurantInfo(String responseJson,Long restaurantId,Long id) throws JsonProcessingException {
            ObjectMapper mapper = new ObjectMapper();
            OcrResponse response = mapper.readValue(responseJson, OcrResponse.class);

            OcrReceiptResult result = response.getImages().get(0).getReceipt().getResult();
            String name = result.getStoreInfo().getName().getText();
            String subName = Optional.ofNullable(result.getStoreInfo().getSubName())
                    .map(OcrSimpleText::getText)
                    .orElse("");
            String combinedName = name + subName;

            String restaurantName = formatRestaurantName(combinedName);

            log.info("restaurantName={}",restaurantName);

            OcrFormattedDate date = result.getPaymentInfo().getDate().getFormatted();
            String visitDate = String.format("%s-%s-%s", date.getYear(), date.getMonth(), date.getDay());

            log.info("visitDate={}",visitDate);

            validateReceipt(restaurantId,restaurantName,visitDate,id);
            return new ResOcrDTO(restaurantName, visitDate);

    }

    private String formatRestaurantName(String raw) {
        // 예: "오리역점(메가MGC커피)" → "메가MGC커피 오리역점"
        if (raw.contains("(") && raw.contains(")")) {
            String inside = raw.replaceAll(".*\\((.*)\\).*", "$1").trim(); // 괄호 안
            String outside = raw.replaceAll("\\(.*\\)", "").trim(); // 괄호 밖
            return inside + " " + outside;
        }
        return raw.trim();
    }


}


