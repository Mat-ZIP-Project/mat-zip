package web.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;


@Service
@Transactional
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final RestaurantImageRepository restaurantImageRepository;
    private final UserLikeRepository userLikeRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public List<RestaurantListResponseDTO> getRestaurants(List<String> categoryList, String regionSigungu, String sortBy, Integer size, Long userId) {
        List<Restaurant> restaurants;

        // 필터링 조건 존재 여부 체크
        boolean hasCategory = categoryList != null && !categoryList.isEmpty();
        boolean hasRegion = regionSigungu != null && !regionSigungu.isBlank();

        // 조건에 따라 식당 목록 조회
        if (hasCategory && hasRegion) {
            restaurants = restaurantRepository.findByCategoryInAndRegionSigungu(categoryList, regionSigungu);
        } else if (hasCategory) {
            restaurants = restaurantRepository.findByCategoryIn(categoryList);
        } else if (hasRegion) {
            restaurants = restaurantRepository.findByRegionSigungu(regionSigungu);
        } else {
            restaurants = restaurantRepository.findAll();
        }

        // 로그인한 유저가 있으면 찜한 식당 ID 목록 조회
        List<Long> likedRestaurantIds;
        if (userId != null) {
            likedRestaurantIds = userLikeRepository.findLikedRestaurantIdsByUserId(userId);
        } else {
            likedRestaurantIds = Collections.emptyList();
        }

        List<RestaurantListResponseDTO> result = restaurants.stream()
                .map(restaurant -> {
                    boolean liked = likedRestaurantIds.contains(restaurant.getRestaurantId());

                    int likeCount = userLikeRepository.countByRestaurant(restaurant);
                    long reviewCount = reviewRepository.countReviewByRestaurant(restaurant);
                    int reservationCount = reservationRepository.countByRestaurant(restaurant);

                    return RestaurantListResponseDTO.builder()
                            .restaurantId(restaurant.getRestaurantId())
                            .restaurantName(restaurant.getRestaurantName())
                            .address(restaurant.getAddress())
                            .regionSido(restaurant.getRegionSido())
                            .regionSigungu(restaurant.getRegionSigungu())
                            .category(restaurant.getCategory())
                            .avgRating(restaurant.getAvgRating())
                            .avgRatingLocal(restaurant.getAvgRatingLocal())
                            .likeCount(likeCount)
                            .reviewCount(reviewCount)
                            .reservationCount(reservationCount)
                            .imageUrl(
                                    restaurantImageRepository.findAllByRestaurant(restaurant).stream()
                                            .map(RestaurantImage::getImageUrl)
                                            .findFirst()
                                            .orElse(null))
                            .liked(liked)
                            .build();
                }).collect(Collectors.toList());

        // 정렬 처리
        switch (sortBy) {
            case "likes":
                result.sort(Comparator.comparingInt(RestaurantListResponseDTO::getLikeCount).reversed());
                break;
            case "reviewCount":
                result.sort(Comparator.comparingLong(RestaurantListResponseDTO::getReviewCount).reversed());
                break;
            case "reservationCount":
                result.sort(Comparator.comparingInt(RestaurantListResponseDTO::getReservationCount).reversed());
                break;
            case "avgRatingLocal":
                result.sort(Comparator.comparingDouble((RestaurantListResponseDTO dto) ->
                        Optional.ofNullable(dto.getAvgRatingLocal()).orElse(0.0)
                ).reversed());
                break;
            default:
                result.sort(Comparator.comparingInt(RestaurantListResponseDTO::getLikeCount).reversed());
        }

        // size가 null이 아니면 제한, 아니면 전체 반환
        if (size != null) {
            return result.stream().limit(size).collect(Collectors.toList());
        } else {
            return result;
        }
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

        int likeCount = userLikeRepository.countByRestaurant(restaurant);
        long reviewCount = reviewRepository.countReviewByRestaurant(restaurant);

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
                .avgRatingLocal(restaurant.getAvgRatingLocal())
                .openTime(restaurant.getOpenTime() != null ? restaurant.getOpenTime().toLocalTime() : null)
                .closeTime(restaurant.getCloseTime() != null ? restaurant.getCloseTime().toLocalTime() : null)
                .menus(menus)
                .imageUrls(imageUrls)
                .likeCount(likeCount)
                .reviewCount(reviewCount)
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

    @Override
    public List<RestaurantListResponseDTO> searchRestaurantsByKeyword(String keyword) {
        List<Restaurant> restaurants = restaurantRepository.findByRestaurantNameContaining(keyword);

        return restaurants.stream()
                .map(restaurant -> {
                    int likeCount = userLikeRepository.countByRestaurant(restaurant);
                    long reviewCount = reviewRepository.countReviewByRestaurant(restaurant);
                    int reservationCount = reservationRepository.countByRestaurant(restaurant);

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
                            .imageUrl(
                                    restaurantImageRepository.findAllByRestaurant(restaurant).stream()
                                            .map(RestaurantImage::getImageUrl)
                                            .findFirst()
                                            .orElse(null))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ResReviewDTO> getReviewsByRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));


        List<Review> reviews = reviewRepository.findByRestaurantAndLocalReviewFalse(restaurant);

        return reviews.stream()
                .map(review -> ResReviewDTO.builder()
                        .reviewId(review.getReviewId())
                        .content(review.getContent())
                        .rating(review.getRating())
                        .reviewedAt(review.getReviewedAt())
                        .visitDate(review.getVisitDate())
                        .localReview(review.isLocalReview())
                        .userNickname(review.getUser().getName())
                        .imageUrls(
                                review.getReviewImages().stream()
                                        .map(image -> "/images/reviews/" + image.getImageName())
                                        .toList()
                        )
                        .build())
                .toList();
    }

    @Override
    public List<ResReviewDTO> getLocalReviewsByRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        // ✅ local_review = true만 가져옴
        List<Review> reviews = reviewRepository.findByRestaurantAndLocalReviewTrue(restaurant);
        System.out.println("로컬 리뷰 개수: " + reviews.size());

        return reviews.stream()
                .map(review -> ResReviewDTO.builder()
                        .reviewId(review.getReviewId())
                        .content(review.getContent())
                        .rating(review.getRating())
                        .reviewedAt(review.getReviewedAt())
                        .visitDate(review.getVisitDate())
                        .localReview(review.isLocalReview())
                        .userNickname(review.getUser().getName())
                        .imageUrls(
                                review.getReviewImages().stream()
                                        .map(image -> "/images/reviews/" + image.getImageName())
                                        .toList()
                        )
                        .build())
                .toList();
    }
    
    @Override
    public List<RestaurantListResponseDTO> getRecommendedByCategory(Long userId) {
        // 1. 사용자 선호 카테고리 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BasicException(ErrorCode.USER_NOT_FOUND));

        String preferenceCategory = user.getPreferenceCategory(); // ex) "한식,양식"
        if (preferenceCategory == null || preferenceCategory.isBlank()) {
            return List.of(); // 선호 카테고리가 없으면 빈 리스트 반환
        }

        List<String> categoryList = List.of(preferenceCategory.split(","));

        // 2. 찜 많은 순으로 상위 20개 가져오기
        List<Restaurant> restaurants = restaurantRepository.findTop20ByCategoryInOrderByLikesDesc(categoryList);

        // 3. 로그인 유저가 찜한 식당 ID 조회
        List<Long> likedRestaurantIds = userLikeRepository.findLikedRestaurantIdsByUserId(userId);

        return convertToRestaurantDTOList(restaurants, likedRestaurantIds);
    }

    @Override
    public List<RestaurantListResponseDTO> getRecommendedByLocalRating() {
        List<Restaurant> restaurants = restaurantRepository.findTop20ByAvgRatingLocalDesc();
        return convertToRestaurantDTOList(restaurants, Collections.emptyList());
    }

    @Override
    public List<RestaurantListResponseDTO> getRecommendedByReservation() {
        List<Restaurant> restaurants = restaurantRepository.findTop20ByReservationCountDesc();
        return convertToRestaurantDTOList(restaurants, Collections.emptyList());
    }

    private List<RestaurantListResponseDTO> convertToRestaurantDTOList(List<Restaurant> restaurants, List<Long> likedRestaurantIds) {
        return restaurants.stream().map(restaurant -> {
            boolean liked = likedRestaurantIds.contains(restaurant.getRestaurantId());

            int likeCount = userLikeRepository.countByRestaurant(restaurant);
            long reviewCount = reviewRepository.countReviewByRestaurant(restaurant);
            int reservationCount = reservationRepository.countByRestaurant(restaurant);

            return RestaurantListResponseDTO.builder()
                    .restaurantId(restaurant.getRestaurantId())
                    .restaurantName(restaurant.getRestaurantName())
                    .address(restaurant.getAddress())
                    .regionSido(restaurant.getRegionSido())
                    .regionSigungu(restaurant.getRegionSigungu())
                    .category(restaurant.getCategory())
                    .avgRating(restaurant.getAvgRating())
                    .avgRatingLocal(restaurant.getAvgRatingLocal())
                    .likeCount(likeCount)
                    .reviewCount(reviewCount)
                    .reservationCount(reservationCount)
                    .imageUrl(
                            restaurantImageRepository.findAllByRestaurant(restaurant).stream()
                                    .map(RestaurantImage::getImageUrl)
                                    .findFirst()
                                    .orElse(null)
                    )
                    .liked(liked)
                    .build();
        }).toList();
    }
    
}
