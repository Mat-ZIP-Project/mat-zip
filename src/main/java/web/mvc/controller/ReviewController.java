package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.mvc.dto.ReqReviewDTO;
import web.mvc.dto.ResLocalDTO;
import web.mvc.dto.ResOcrDTO;
import web.mvc.security.CustomUserDetails;
import web.mvc.service.ReceiptOcrService;
import web.mvc.service.RestaurantReviewService;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReceiptOcrService receiptOcrService;
    private final RestaurantReviewService restaurantReviewService;

    /**
     * 영수증 인증하기
     * @param image
     * @param restaurantId
     * @param userDetails
     * @return
     */
    @PostMapping("/ocr/{restaurantId}")
    public ResponseEntity<ResOcrDTO> parseReceipt(@RequestParam MultipartFile image, @PathVariable Long restaurantId,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        ResOcrDTO result = receiptOcrService.parseReceipt(image,restaurantId,userDetails.getUser().getId());
        return ResponseEntity.ok(result);
    }

    /**
     * 중복리뷰 여부 , 로컬리뷰 여부 확인하기
     */
    @GetMapping("/{restaurantId}/{visitDate}")
    public ResponseEntity<ResLocalDTO> checkReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @PathVariable Long restaurantId,@PathVariable String visitDate){
        ResLocalDTO result= restaurantReviewService.checkReview(userDetails.getUser().getId(),restaurantId,visitDate);

        return ResponseEntity.ok(result);
    }


    /**
     * 리뷰 작성하기
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createReview( @RequestPart("review") ReqReviewDTO reqReviewDTO,
                                                @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                                @AuthenticationPrincipal CustomUserDetails userDetails){
        String result = restaurantReviewService.createReview(reqReviewDTO,

                images ==null? Collections.emptyList() : images,userDetails.getUser().getId());

        return ResponseEntity.ok(result);
    }

    /**
     * 리뷰 삭제하기
     */
    @DeleteMapping("/{reviewId}")
    public void deleteReview(@PathVariable Long reviewId){
        restaurantReviewService.deleteReview(reviewId);
    }

}
