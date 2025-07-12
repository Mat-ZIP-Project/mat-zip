package web.mvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import web.mvc.domain.*;
import web.mvc.dto.*;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OwnerServiceImpl implements OwnerService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantImageRepository restaurantImageRepository;
    private final S3Service s3Service;
    private final ModelMapper modelMapper;

    // *** AWS S3 폴더 경로 ***
    private static final String RESTAURANT_FOLDER = "main";

    /** 업주의 식당 정보 조회 */
    @Override
    @Transactional(readOnly = true)
    public RestaurantInfoResponse getRestaurantInfo(String userId) {
        // userId와 연결돼있는 restaurant 가져오기
        Restaurant restaurant = restaurantRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        // Entity -> dto 매핑
        RestaurantInfoResponse response = modelMapper.map(restaurant, RestaurantInfoResponse.class);

        // 매핑되지 않는 특수 필드 별도 설정
        if (restaurant.getOpenTime() != null) {
            response.setOpenTime(restaurant.getOpenTime().toLocalTime());
        }
        if (restaurant.getCloseTime() != null) {
            response.setCloseTime(restaurant.getCloseTime().toLocalTime());
        }

        // 이미지 URL 목록 조회 (대표이미지 먼저 정렬)
        List<String> imageUrls = restaurantImageRepository.findAllByRestaurant(restaurant)
                .stream()
                .map(RestaurantImage::getImageUrl)
                .collect(Collectors.toList());
        response.setImageUrls(imageUrls);

        // 사업자번호 설정
        response.setBusinessNumber(restaurant.getOwner().getBusinessNumber());

        return response;
    }

    /**
     * 식당 기본 정보 수정
     */
    @Override
    public RestaurantInfoResponse updateRestaurantInfo(String userId, RestaurantUpdateRequest request) {
        Restaurant restaurant = restaurantRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        // DTO -> Entity 매핑
        modelMapper.map(request, restaurant);

        // LocalTime -> Time 변환
        if (request.getOpenTime() != null) {
            restaurant.setOpenTime(Time.valueOf(request.getOpenTime()));
        }
        if (request.getCloseTime() != null) {
            restaurant.setCloseTime(Time.valueOf(request.getCloseTime()));
        }
        restaurantRepository.save(restaurant);

        log.info("식당 정보 수정 완료: 업주 ID={}, 식당 ID={}", userId, restaurant.getRestaurantId());

        // 수정된 정보 반환
        return getRestaurantInfo(userId);
    }

    /**
     * 식당 이미지 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<RestaurantImageResponse> getRestaurantImages(String userId) {
        Restaurant restaurant = restaurantRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        // Entity List -> DTO List 매핑
        return restaurantImageRepository.findAllByRestaurant(restaurant)
                .stream()
                .map(image -> modelMapper.map(image, RestaurantImageResponse.class))
                .collect(Collectors.toList());
    }

    /**
     * 업주의 식당 이미지 업로드
     * - 최대 9개 제한
     * - 첫 번째 이미지 업로드 시 대표이미지가 없으면 자동 설정
     */
    @Override
    public List<RestaurantImageResponse> uploadRestaurantImages(String userId, List<MultipartFile> images) {
        Restaurant restaurant = restaurantRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        // 현재 이미지 개수 확인 (최대 9개 제한)
        int currentImageCount = restaurantImageRepository.countByRestaurant(restaurant);
        if (currentImageCount + images.size() > 9) {
            throw new BasicException(ErrorCode.IMAGE_LIMIT_EXCEEDED);
        }

        // 대표이미지 존재 여부 확인
        boolean hasMainImage = restaurantImageRepository.findByRestaurantAndIsMainTrue(restaurant).isPresent();

        List<RestaurantImageResponse> responses = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            MultipartFile image = images.get(i);

            // S3에 이미지 업로드
            String imageUrl = s3Service.uploadImageForRestaurant(userId, image, RESTAURANT_FOLDER);

            // 첫 번째 이미지이고 대표이미지가 없으면 자동으로 대표이미지 설정
            boolean isMainImage = !hasMainImage && i == 0;

            RestaurantImage restaurantImage = RestaurantImage.builder()
                    .restaurant(restaurant)
                    .imageUrl(imageUrl)
                    .isMain(isMainImage)
                    .build();

            RestaurantImage savedImage = restaurantImageRepository.save(restaurantImage);

            // Entity -> DTO 매핑
            RestaurantImageResponse response = modelMapper.map(savedImage, RestaurantImageResponse.class);
            responses.add(response);

            // 첫 번째 이미지가 대표이미지로 설정되면 플래그 업데이트
            if (isMainImage) {
                hasMainImage = true;
            }
        }
        log.info("식당 이미지 업로드 완료: 업주 ID={}, 식당 ID={}, 업로드 개수={}",
                userId, restaurant.getRestaurantId(), images.size());

        return responses;
    }

    /**
     * 업주의 식당 이미지 삭제
     * - 대표이미지 삭제 시 자동으로 다른 이미지를 대표이미지로 설정
     */
    @Override
    public void deleteRestaurantImage(String userId, Long imageId) {
        Restaurant restaurant = restaurantRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        RestaurantImage image = restaurantImageRepository.findById(imageId)
                .orElseThrow(() -> new BasicException(ErrorCode.IMAGE_NOT_FOUND));

        // 해당 식당의 이미지인지 확인
        if (!image.getRestaurant().getRestaurantId().equals(restaurant.getRestaurantId())) {
            throw new BasicException(ErrorCode.ACCESS_DENIED);
        }

        boolean wasMainImage = image.getIsMain();

        // S3에서 이미지 삭제
        s3Service.deleteImage(image.getImageUrl());

        // DB에서 이미지 삭제
        restaurantImageRepository.delete(image);

        // 대표이미지를 삭제한 경우 다른 이미지를 대표이미지로 자동 설정
        if (wasMainImage) {
            List<RestaurantImage> remainingImages = restaurantImageRepository.findAllByRestaurant(restaurant);
            if (!remainingImages.isEmpty()) {
                RestaurantImage newMainImage = remainingImages.get(0);
                newMainImage.setIsMain(true);
                restaurantImageRepository.save(newMainImage);
                log.info("대표이미지 자동 설정: 이미지 ID={}", newMainImage.getImageId());
            }
        }
        log.info("식당 이미지 삭제 완료: 업주 ID={}, 이미지 ID={}", userId, imageId);
    }

    /**
     * 업주의 식당 대표이미지 설정
     * - 기존 대표이미지 해제 후 새로운 대표이미지 설정
     */
    @Override
    public void setMainImage(String userId, Long imageId) {
        Restaurant restaurant = restaurantRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        RestaurantImage newMainImage = restaurantImageRepository.findById(imageId)
                .orElseThrow(() -> new BasicException(ErrorCode.IMAGE_NOT_FOUND));

        // 해당 식당의 이미지인지 확인
        if (!newMainImage.getRestaurant().getRestaurantId().equals(restaurant.getRestaurantId())) {
            throw new BasicException(ErrorCode.ACCESS_DENIED);
        }

        // 기존 대표이미지가 있으면 해제
        Optional<RestaurantImage> currentMainImage = restaurantImageRepository.findByRestaurantAndIsMainTrue(restaurant);
        if (currentMainImage.isPresent()) {
            currentMainImage.get().setIsMain(false);
            restaurantImageRepository.save(currentMainImage.get());
        }

        // 새로운 대표이미지 설정
        newMainImage.setIsMain(true);
        restaurantImageRepository.save(newMainImage);

        log.info("대표이미지 설정 완료: 업주 ID={}, 식당 ID={}, 이미지 ID={}",
                userId, restaurant.getRestaurantId(), imageId);
    }
}