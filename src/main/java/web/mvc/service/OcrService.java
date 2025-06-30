package web.mvc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import web.mvc.config.OcrConfig;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OcrService {

    private final OcrConfig ocrConfig;

    /**
     * OCR 처리 후 해당 식당명이 영수증에 포함되어 있는지 확인
     * @param imagePath 영수증 이미지 경로
     * @param restaurantName 사이트 식당명
     * @return 식당명이 OCR 결과에 포함되어 있으면 true
     * @throws Exception 예외 발생 시
     */
    public boolean verifyStoreNameInOcr(String imagePath, String restaurantName) throws Exception {
        // 1. 이미지 파일 base64 인코딩
        File file = new File(imagePath);
        FileInputStream fis = new FileInputStream(file);
        byte[] imageBytes = fis.readAllBytes();
        fis.close();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // 2. 요청 JSON 구성
        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("format", "jpg");
        imageMap.put("name", "receipt");
        imageMap.put("data", base64Image);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("version", "V2");
        requestBody.put("requestId", UUID.randomUUID().toString());
        requestBody.put("timestamp", System.currentTimeMillis());
        requestBody.put("images", Collections.singletonList(imageMap));

        // 3. 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-OCR-SECRET", ocrConfig.getSecretKey());

        // 4. 요청 전송
        HttpEntity<String> entity = new HttpEntity<>(new ObjectMapper().writeValueAsString(requestBody), headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                ocrConfig.getInvokeUrl(),
                HttpMethod.POST,
                entity,
                String.class
        );

        String resultJson = response.getBody();

        // 5. OCR 텍스트 추출 후 식당명 포함 여부 확인
        List<String> extractedTexts = extractAllInferTexts(resultJson);
        for (String text : extractedTexts) {
            if (text.contains(restaurantName)) {
                return true;
            }
        }
        return false;
    }

    private List<String> extractAllInferTexts(String ocrJson) {
        List<String> texts = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(ocrJson);

            for (JsonNode image : root.path("images")) {
                for (JsonNode field : image.path("fields")) {
                    String text = field.path("inferText").asText();
                    if (!text.isEmpty()) {
                        texts.add(text);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return texts;
    }
}
