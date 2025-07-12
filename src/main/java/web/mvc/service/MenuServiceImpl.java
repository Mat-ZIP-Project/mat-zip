package web.mvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import web.mvc.domain.Menu;
import web.mvc.domain.Restaurant;
import web.mvc.dto.MenuRequest;
import web.mvc.dto.MenuResponse;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.MenuRepository;
import web.mvc.repository.RestaurantRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MenuServiceImpl implements MenuService {
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final S3Service s3Service;

    // *** AWS S3 폴더 경로 ***
    private static final String MENU_FOLDER = "menus";

    /**
     * 업주가 등록한 식당의 모든 메뉴 조회
     */
    @Override
    public List<MenuResponse> getMenus(String userId) {
        // 업주 메뉴 목록 조회
        List<Menu> menus = menuRepository.findByOwnerUserId(userId);
        return menus.stream()
                .map(MenuResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 새로운 메뉴 등록
     */
    @Override
    @Transactional
    public MenuResponse createMenu(String userId, MenuRequest request, MultipartFile image) {
        System.out.println("MenuService createMenu 호출");
        // 입력 검증
        validateMenuRequest(request);

        // 업주 식당 조회
        Restaurant restaurant = restaurantRepository.findByOwnerUserId(userId)
                .orElseThrow(() ->  new BasicException(ErrorCode.FORBIDDEN)); //"권한이 없습니다."

        // 이미지가 제공되면 S3에 업로드
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            //String dir = getMenuImageDirectory(restaurant.getRestaurantId());
            imageUrl = s3Service.uploadImage(userId, image, MENU_FOLDER);
        }

        // 메뉴 엔티티 생성 및 저장
        Menu menu = Menu.builder()
                .menuName(request.getMenuName().trim())
                .price(request.getPrice())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .imageUrl(imageUrl)
                .restaurant(restaurant)
                .build();

        Menu savedMenu = menuRepository.save(menu);
        log.info("메뉴 등록 완료: 업주={}, 메뉴ID={}, 메뉴명={}", userId, savedMenu.getMenuId(), savedMenu.getMenuName());

        return MenuResponse.fromEntity(savedMenu);
    }

    /**
     * 기존 메뉴 수정
     */
    @Override
    @Transactional
    public MenuResponse updateMenu(String userId, Long menuId, MenuRequest request, MultipartFile image) {
        // 입력 검증
        validateMenuRequest(request);

        // 메뉴 및 업주 소유권 검증
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND));
        validateOwnership(menu, userId);

        // 기존 이미지 삭제 후 새 이미지 업로드
        if (image != null && !image.isEmpty()) {
            if (menu.getImageUrl() != null) {
                try {
                    s3Service.deleteImage(menu.getImageUrl());
                } catch (Exception e) {
                    log.warn("기존 이미지 삭제 실패: {}", menu.getImageUrl(), e);
                }
            }
            //String dir = getMenuImageDirectory(menu.getRestaurant().getRestaurantId());
            String newUrl = s3Service.uploadImage(userId, image, MENU_FOLDER);
            menu.setImageUrl(newUrl);
        }

        // 메뉴 정보 업데이트
        menu.setMenuName(request.getMenuName().trim());
        menu.setPrice(request.getPrice());
        menu.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Menu savedMenu = menuRepository.save(menu);
        log.info("메뉴 수정 완료: 업주={}, 메뉴ID={}", userId, menuId);

        return MenuResponse.fromEntity(savedMenu);
    }

    /**
     * 메뉴 삭제
     */
    @Override
    @Transactional
    public void deleteMenu(String userId, Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BasicException(ErrorCode.NOT_FOUND)); //"메뉴를 찾을 수 없습니다."
        validateOwnership(menu, userId);

        // 이미지 삭제
        if (menu.getImageUrl() != null) {
            try {
                s3Service.deleteImage(menu.getImageUrl());
            } catch (Exception e) {
                log.warn("메뉴 이미지 삭제 실패: {}", menu.getImageUrl(), e);
            }
        }
        menuRepository.delete(menu);
        log.info("메뉴 삭제 완료: 업주={}, 메뉴ID={}", userId, menuId);
    }

    // 공통 검증 메서드
    private void validateMenuRequest(MenuRequest request) {
        if (request.getMenuName() == null || request.getMenuName().trim().isEmpty()) {
            throw new BasicException(ErrorCode.BAD_REQUEST); //"메뉴명은 필수입니다."
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new BasicException(ErrorCode.BAD_REQUEST); //"가격은 0보다 큰 값이어야 합니다."
        }
    }

    private void validateOwnership(Menu menu, String userId) {
        if (!menu.getRestaurant().getOwner().getUser().getUserId().equals(userId)) {
            throw new BasicException(ErrorCode.FORBIDDEN); //"해당 요청에 대한 권한이 없습니다."
        }
    }
    /** 메뉴 이미지 디렉토리 경로 생성 메서드 */
//    private String getMenuImageDirectory(Long restaurantId) {
//        return String.format("%s/%d/%s", RESTAURANT_FOLDER, restaurantId, MENU_FOLDER);
//    }


}
