package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_spots")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseSpots {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long spotId;
    private int visitOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurantId")
    private Restaurant restaurant;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="courseId")
    private CustomCourse customCourse;

}
