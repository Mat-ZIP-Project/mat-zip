package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
    @Column(name = "restaurant_id")
    private Long restaurantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private OwnerInfo owner; // 식당 주인

    @Column(name = "restaurant_name")
    private String restaurantName;
    private String address;
    @Column(name = "region_sido")
    private String regionSido; // 서울특별시, 경기도
    @Column(name = "region_sigungu")
    private String regionSigungu; // 서울시 내 각 구 (25개) , 성남시, 용인시, 수원시
    private double latitude;   //위도
    private double longitude;  //경도

    @Column(name = "avg_rating", nullable = false)
    private double avgRating = 0.0;
    @Column(name = "avg_rating_local", nullable = false)
    private double avgRatingLocal = 0.0;

    private String phone;

    private String category; // 카테고리 : 한식, 일식, 중식, 양식, 카페
    private String descript;

    @Column(name = "open_time")
    private Time openTime;
    @Column(name = "close_time")
    private Time closeTime;

    @Column(name = "is_advertised")
    private Boolean isAdvertised = false;

    @Column(name = "max_waiting_limit")
    private Integer maxWaitingLimit;

    @CreationTimestamp
    @Column(name = "create_at")
    private LocalDateTime createAt = LocalDateTime.now();
}
