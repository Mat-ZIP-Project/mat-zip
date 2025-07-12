package web.mvc.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantImageResponse {
    private Long imageId;
    private String imageUrl;
    private Boolean isMain;
}
