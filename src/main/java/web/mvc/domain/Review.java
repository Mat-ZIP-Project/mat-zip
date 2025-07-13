package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    @Column(name = "review_id")
    private Long reviewId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int rating;

    @CreationTimestamp
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "local_review", nullable = false)
    private boolean localReview = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private User user;


    @OneToMany(mappedBy = "review" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> reviewImages;


}
