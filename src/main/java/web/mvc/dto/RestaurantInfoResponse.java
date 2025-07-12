package web.mvc.dto;

import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** 식당 업주 정보 조회용 */
public class RestaurantInfoResponse {
    private Long restaurantId;
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
    private List<String> imageUrls;

    // 업주 정보
    private String businessNumber;
}
