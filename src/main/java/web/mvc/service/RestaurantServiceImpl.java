package web.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.*;
import web.mvc.dto.*;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.*;
import web.mvc.security.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final RestaurantImageRepository restaurantImageRepository;
    private final UserLikeRepository userLikeRepository;
    private final UserRepository userRepository;

    @Override
    public List<RestaurantListResponseDTO> getRestaurants(String category, String regionSigungu, String sortBy) {
        List<Restaurant> restaurants;

        // 필터링 조건에 따라 식당 조회
        if (category != null && regionSigungu != null) {
            restaurants = restaurantRepository.findByCategoryAndRegionSigungu(category, regionSigungu);
        } else if (category != null) {
            restaurants = restaurantRepository.findByCategory(category);
        } else if (regionSigungu != null) {
            restaurants = restaurantRepository.findByRegionSigungu(regionSigungu);
        } else {
            restaurants = restaurantRepository.findAll(); // 전체 조회
        }

        List<RestaurantListResponseDTO> result = restaurants.stream()
                .map(restaurant -> {
                    int likeCount = userLikeRepository.countByRestaurant(restaurant);
                    int reviewCount = 0;       // 리뷰 수 연동
                    int reservationCount = 0;  // 예약 수 연동

                    return RestaurantListResponseDTO.builder()
                            .restaurantId(restaurant.getRestaurantId())
                            .restaurantName(restaurant.getRestaurantName())
                            .address(restaurant.getAddress())
                            .regionSido(restaurant.getRegionSido())
                            .regionSigungu(restaurant.getRegionSigungu())
                            .category(restaurant.getCategory())
                            .avgRating(restaurant.getAvgRating())
                            .likeCount(likeCount)
                            .reviewCount(reviewCount)
                            .reservationCount(reservationCount)
                            .thumbnailImageUrl(
                                    restaurantImageRepository.findAllByRestaurant(restaurant).stream()
                                            .map(RestaurantImage::getImageUrl)
                                            .findFirst()
                                            .orElse(null))
                            .build();
                }).collect(Collectors.toList());

        // 정렬 처리
        switch (sortBy) {
            case "likes": // 가게 좋아요 순
                result.sort((r1, r2) -> Integer.compare(r2.getLikeCount(), r1.getLikeCount()));
                break;
            case "reviewCount": // 리뷰 횟수 순
                result.sort((r1, r2) -> Integer.compare(r2.getReviewCount(), r1.getReviewCount()));
                break;
            case "reservationCount": // 예약 횟수 순
                result.sort((r1, r2) -> Integer.compare(r2.getReservationCount(), r1.getReservationCount()));
                break;
            default:
                // 기본: likes 기준
                result.sort((r1, r2) -> Integer.compare(r2.getLikeCount(), r1.getLikeCount()));
        }

        return result;
    }


    @Override
    public RestaurantDetailDTO getRestaurantDetail(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        List<MenuDTO> menus = menuRepository.findAllByRestaurant(restaurant)
                .stream().map(menu -> MenuDTO.builder()
                        .menuId(menu.getMenuId())
                        .menuName(menu.getMenuName())
                        .price(menu.getPrice())
                        .description(menu.getDescription())
                        .imageUrl(menu.getImageUrl())
                        .build()
                ).collect(Collectors.toList());

        List<String> imageUrls = restaurantImageRepository.findAllByRestaurant(restaurant)
                .stream().map(RestaurantImage::getImageUrl).collect(Collectors.toList());

        return RestaurantDetailDTO.builder()
                .restaurantId(restaurant.getRestaurantId())
                .restaurantName(restaurant.getRestaurantName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .regionSido(restaurant.getRegionSido())
                .regionSigungu(restaurant.getRegionSigungu())
                .category(restaurant.getCategory())
                .descript(restaurant.getDescript())
                .avgRating(restaurant.getAvgRating())
                .openTime(restaurant.getOpenTime() != null ? restaurant.getOpenTime().toLocalTime() : null)
                .closeTime(restaurant.getCloseTime() != null ? restaurant.getCloseTime().toLocalTime() : null)
                .menus(menus)
                .imageUrls(imageUrls)
                .likeCount(userLikeRepository.countByRestaurant(restaurant))
                .reviewCount(0) // 리뷰 수 추후 연동
                .build();
    }

    @Override
    public void toggleLikeRestaurant(Long userId, Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BasicException(ErrorCode.USER_NOT_FOUND));

        Optional<UserLike> existing = userLikeRepository.findByUserAndRestaurant(user, restaurant);

        if (existing.isPresent()) {
            userLikeRepository.delete(existing.get()); // 이미 찜한 경우 해제
        } else {
            userLikeRepository.save(UserLike.builder()
                    .user(user)
                    .restaurant(restaurant)
                    .likedAt(LocalDateTime.now())
                    .build()); // 아직 찜 안한 경우 저장
        }


    }

}
