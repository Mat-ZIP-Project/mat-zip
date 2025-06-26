package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;



@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true, nullable = false)
    private String userId;

    private String password;
    private String name;

    @Column(unique = true, nullable = false)
    private String phone;

    private String role; // USER, OWNER

    private Integer pointBalance;

    private Boolean gpsVerified = false;
    private Boolean noShow = false;

    private String userStatus;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

}
