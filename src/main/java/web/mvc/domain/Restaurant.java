package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    private String restaurantName;
    private String address;
    private String regionSido;
    private String regionSigungu;
    private double avgRating;
    private String phone;
    private String category;
    private String descript;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isAdvertised;
    private Integer maxWaitingLimit;

    private LocalDateTime createAt;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private OwnerInfo owner;
}
