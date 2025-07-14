package web.mvc.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantListResponseDTO {
    private Long restaurantId;
    private String restaurantName;
    private String address;
    private String regionSido;
    private String regionSigungu;
    private double avgRating;
    private double avgRatingLocal;
    private String category;
    private String thumbnailImageUrl; // 대표 이미지
    private int likeCount;     // 찜 수
    private int reviewCount;
    private int reservationCount;
}
