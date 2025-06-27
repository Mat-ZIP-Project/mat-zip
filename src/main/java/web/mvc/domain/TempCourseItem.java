package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "temp_course_item")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TempCourseItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long temp_id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurantId")
    private Restaurant restaurant;

    private int visitOrder;

}
