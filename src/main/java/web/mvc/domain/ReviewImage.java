package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_images")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;
    private String imageName;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "reviewId")
    private Review review;

    @CreationTimestamp
    private LocalDateTime uploadedAt;


}
