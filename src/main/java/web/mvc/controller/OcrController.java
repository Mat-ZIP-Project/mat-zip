package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import web.mvc.service.OcrService;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    /**
     * 영수증 OCR 이미지에서 식당명이 존재하는지 확인
     * @param imagePath 영수증 이미지 경로
     * @param restaurantName 비교할 식당 이름
     * @return 검증 결과 메시지
     */
    @PostMapping("/verify")
    public String verifyRestaurantName(@RequestParam String imagePath,
                                       @RequestParam String restaurantName) {
        try {
            boolean matched = ocrService.verifyStoreNameInOcr(imagePath, restaurantName);
            return matched ?
                    "✅ 식당명이 일치하여 리뷰 작성이 가능합니다." :
                    "❌ 식당명이 일치하지 않아 리뷰 작성이 제한됩니다.";
        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ 영수증 스캔 중 오류가 발생했습니다.: " + e.getMessage();
        }
    }
}
