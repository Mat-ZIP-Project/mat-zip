package web.mvc.service;

import org.springframework.web.multipart.MultipartFile;
import web.mvc.dto.ReqReviewDTO;
import web.mvc.dto.ResLocalDTO;

import java.util.List;

public interface RestaurantReviewService {
    /**
     * 중복리뷰 여부, 로컬리뷰 여부 확인하기
     */
    ResLocalDTO checkReview(Long id, Long restaurantId, String visitDate);
    /**
     * 리뷰작성하기
     */
    String createReview(ReqReviewDTO reqReviewDTO, List<MultipartFile> images, Long id);

    /**
     * 리뷰 삭제하기
     */
    void deleteReview( Long reviewId);

}
