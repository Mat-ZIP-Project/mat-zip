package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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
    @CreationTimestamp
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "customCourse", cascade = CascadeType.ALL)
    private List<CourseSpots> courseSpotsList;
}
