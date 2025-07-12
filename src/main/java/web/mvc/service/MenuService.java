package web.mvc.service;

import org.springframework.web.multipart.MultipartFile;
import web.mvc.dto.MenuRequest;
import web.mvc.dto.MenuResponse;

import java.util.List;

public interface MenuService {
    /** 업주가 등록한 식당의 모든 메뉴 조회 */
    List<MenuResponse> getMenus(String userId);

    /** 새로운 메뉴 등록 */
    MenuResponse createMenu(String userId, MenuRequest request, MultipartFile image);

    /** 기존 메뉴 수정 */
    MenuResponse updateMenu(String userId, Long menuId, MenuRequest request, MultipartFile image);

    /** 메뉴 삭제 */
    void deleteMenu(String userId, Long menuId);
}
