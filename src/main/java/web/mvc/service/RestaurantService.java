package web.mvc.service;

import web.mvc.dto.ResReviewDTO;
import web.mvc.dto.RestaurantListResponseDTO;
import web.mvc.dto.RestaurantDetailDTO;

import java.util.List;

public interface RestaurantService {

    /**
     * [식당 목록 조회]
     * 사용자의 필터(카테고리, 지역)와 정렬 기준에 따라 식당 목록을 조회합니다.
     */
    List<RestaurantListResponseDTO> getRestaurants(List<String> category, String regionSigungu, String sortBy);

    /**
     * [식당 상세 정보 조회]
     * 식당 ID를 기반으로 해당 식당의 상세 정보를 조회합니다.
     */
    RestaurantDetailDTO getRestaurantDetail(Long restaurantId);

    /**
     * [식당 찜 등록 또는 취소]
     * 사용자가 특정 식당을 찜했는지 여부에 따라 토글 방식으로 찜을 등록하거나 취소합니다.
     */
    void toggleLikeRestaurant(Long userId, Long restaurantId);

    /**
     * [식당 키워드 검색]
     * 식당 이름에 특정 키워드가 포함된 식당 목록을 조회합니다.
     */
    List<RestaurantListResponseDTO> searchRestaurantsByKeyword(String keyword);

    List<ResReviewDTO> getReviewsByRestaurant(Long restaurantId);

    List<ResReviewDTO> getLocalReviewsByRestaurant(Long restaurantId);


}
