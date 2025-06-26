package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "custom_courses")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id")
    private User user;

    private String title;
    private LocalDateTime createdAt;
}
