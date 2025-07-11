package web.mvc.dto;

import lombok.*;
import java.time.LocalTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantDetailDTO {
    private Long restaurantId;
    private String restaurantName;
    private String address;
    private String phone;
    private String regionSido;
    private String regionSigungu;
    private String category;
    private String descript;

    private double avgRating;
    private double avgRatingLocal;
    private LocalTime openTime;
    private LocalTime closeTime;

    private List<MenuDTO> menus;
    private List<String> imageUrls;
    private int likeCount;
    private int reviewCount;
}
