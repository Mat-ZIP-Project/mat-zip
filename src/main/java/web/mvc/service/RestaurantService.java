package web.mvc.service;

import web.mvc.dto.RestaurantListResponseDTO;
import web.mvc.dto.RestaurantDetailDTO;

import java.util.List;

public interface RestaurantService {

    /**
     * 식당 전체 목록 또는 조건 검색
     */
    public List<RestaurantListResponseDTO> getRestaurants(String category, String regionSigungu, String sortBy);

    /**
     * 식당 상세 조회
     */
    RestaurantDetailDTO getRestaurantDetail(Long restaurantId);

    /**
     * 식당 찜 등록 및 취소 (toggle)
     */
    void toggleLikeRestaurant(Long userId, Long restaurantId);

    /**
     * 식당 찜 취소
     */
//    void unlikeRestaurant(Long userId, Long restaurantId);

    List<RestaurantListResponseDTO> searchRestaurantsByKeyword(String keyword);

}
