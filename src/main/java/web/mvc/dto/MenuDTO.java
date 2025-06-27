package web.mvc.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuDTO {
    private Long menuId;
    private String menuName;
    private int price;
    private String description;
    private String imageUrl;
}
