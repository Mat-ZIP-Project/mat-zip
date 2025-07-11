package web.mvc.dto;

import lombok.*;
import web.mvc.domain.Menu;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponse {
    private Long menuId;
    private String menuName;
    private int price;
    private String description;
    private String imageUrl;

    /**
     * 엔티티를 DTO로 변환하는 팩토리 메서드
     */
    public static MenuResponse fromEntity(Menu menu) {
        return new MenuResponse(
                menu.getMenuId(),
                menu.getMenuName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getImageUrl()
        );
    }
}
