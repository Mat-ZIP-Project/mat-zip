package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private OwnerInfo owner; // 식당 주인

    private String restaurantName;
    private String address;
    private String regionSido; // 서울특별시, 경기도
    private String regionSigungu; // 서울시 내 각 구 (25개) , 성남시, 용인시, 수원시

    private double avgRating = 0.0;
    private String phone;

    private String category; // 카테고리 : 한식, 일식, 중식, 양식, 카페
    private String descript;

    private Time openTime;
    private Time closeTime;

    private Boolean isAdvertised = false;

    private Integer maxWaitingLimit;

    private LocalDateTime createAt = LocalDateTime.now();
}
