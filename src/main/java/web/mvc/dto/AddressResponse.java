package web.mvc.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private String addressName;     // 전체 주소명
    private String regionSido;      // 시도명 (서울특별시)
    private String regionSigungu;   // 시군구명 (강남구)
    private double latitude;        // 위도
    private double longitude;       // 경도
}
