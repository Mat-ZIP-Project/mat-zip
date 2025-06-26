package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    private String content;
    private int rating;

    @Column(name = "local_review")
    private boolean localReview;

    @Column(name = "restaurant_id")
    private Long restaurantId;
}
