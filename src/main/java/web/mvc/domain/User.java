package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    private String password;
    private String name;
    private String phone;
    private String role;
    private Integer pointBalance;
    private Boolean noShow;
    private Boolean gpsVerified;
    private String userStatus;
    private String userGrade;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
