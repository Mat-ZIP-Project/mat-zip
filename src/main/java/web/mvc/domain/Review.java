package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(name = "uniq_user_restaurant_visit", columnNames = {"id", "restaurant_id", "visit_date"}))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int rating;

    private String category;

    private boolean siteReview;

    @Column(nullable = false)
    private LocalDateTime reviewedAt;

    @Column(nullable = false)
    private LocalDate visitDate;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne
    @JoinColumn(name = "id")
    private User user;

    private Long sourceId;
}
