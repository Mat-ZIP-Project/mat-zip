package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantLikeDetailDto {
    private Long likeId;
    private LocalDateTime likedAt;

    private Long restaurantId;
    private String restaurantName;
    private String address;
    private String category;
    private double avgRating;
    private String phone;
    private String descript;

    private Time openTime;
    private Time closeTime;
}
