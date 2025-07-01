package web.mvc.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResRestaurantDTO {
    private Long restaurantId;
    private String restaurantName;
    private String regionSido;
    private String regionSigungu;
    private double longitude;
    private double latitude;
    private double avgRating;
    private String category;
    private String thumbnailImageUrl;
    private int reviewCount;

}
