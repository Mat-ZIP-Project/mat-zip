package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "meetup_reviews")
public class MeetupReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meetup_review_id")
    private Long meetupReviewId;

    @Column(name = "review_content", columnDefinition = "TEXT")
    private String reviewContent;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "join_id", nullable = false)
    private MeetupParticipant meetupParticipant;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        //리뷰가 언제 작성됐는지 기록
    }
}
