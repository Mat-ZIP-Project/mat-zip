package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Time;

@Getter
@Setter
public class SignupOwnerRequest {
    // User 필드들
    private String userId;
    private String password;
    private String name;
    private String phone;
    private Boolean termsAgreed;
    private Boolean privacyAgreed;
    private String role = "ROLE_OWNER";

    private String businessNumber;

    // 식당 정보
    private String restaurantName;
    private String address;
    private String regionSido;
    private String regionSigungu;
    private Double latitude;
    private Double longitude;

    private String restaurantPhone; // 식당 연락처

    private String category; // 한식, 중식, 일식, 양식, 카페
    private String descript;

    private Time openTime; // "HH:mm:ss" 형태
    private Time closeTime;

    private Integer maxWaitingLimit;
}
