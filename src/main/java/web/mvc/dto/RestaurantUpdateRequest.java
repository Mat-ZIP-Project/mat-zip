package web.mvc.dto;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** 식당 정보 수정용 */
public class RestaurantUpdateRequest {
    private String restaurantName;
    private String address;
    private String regionSido;
    private String regionSigungu;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String category;
    private String descript;

    private LocalTime openTime;
    private LocalTime closeTime;

    private Integer maxWaitingLimit;
}