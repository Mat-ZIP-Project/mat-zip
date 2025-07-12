package web.mvc.service;

import org.springframework.web.multipart.MultipartFile;
import web.mvc.dto.*;
import java.util.List;

public interface OwnerService {

    /** 업주의 식당 정보 조회 */
    RestaurantInfoResponse getRestaurantInfo(String userId);

    /** 식당 기본 정보 수정 */
    RestaurantInfoResponse updateRestaurantInfo(String userId, RestaurantUpdateRequest request);

    /** 식당 이미지 목록 조회 */
    List<RestaurantImageResponse> getRestaurantImages(String userId);

    /** 식당 이미지 업로드 */
    List<RestaurantImageResponse> uploadRestaurantImages(String userId, List<MultipartFile> images);

    /** 식당 이미지 삭제 */
    void deleteRestaurantImage(String userId, Long imageId);

    /** 업주의 식당 대표이미지 설정 */
    void setMainImage(String userId, Long imageId);
}