package web.mvc.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "review_likes")
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // FK: users.id

    private Long reviewId; // FK: reviews.review_id

    @Column(name = "liked_at")
    private java.sql.Timestamp likedAt = new java.sql.Timestamp(System.currentTimeMillis());
}