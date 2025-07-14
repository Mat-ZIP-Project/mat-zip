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
    private double avgRatingLocal;
    private String category;
    private String thumbnailImageUrl;
    private int likeCount;     // 찜 수
    private long reviewCount;
    private int reservationCount;

}
