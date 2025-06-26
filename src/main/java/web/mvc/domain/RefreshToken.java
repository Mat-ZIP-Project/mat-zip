package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId                   // 엔티티의 PK(id)를 User 엔티티의 PK와 동일하게 매핑
    @JoinColumn(name = "id")
    private User user;

    @Column(unique = true, nullable = false)
    private String token;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
