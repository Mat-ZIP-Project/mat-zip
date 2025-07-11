package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "restaurant_images")
public class RestaurantImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private String imageUrl;

    @Column(name = "is_main", nullable = false)
    private Boolean isMain = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
}
