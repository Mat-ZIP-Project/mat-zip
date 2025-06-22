package web.mvc.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "local_badges", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id", "region_name"})})
public class UserLocalBadge {
    public static final long VALID_PERIOD_DAYS = 60L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authId;

    private String regionName;
    private int authCount;

    @CreationTimestamp
    private LocalDateTime certifiedAt;

    private LocalDateTime validUntil;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id")
    private User user;

    @PrePersist
    public void setDefaultValidUntil(){
        if(certifiedAt==null){
            certifiedAt=LocalDateTime.now();
        }
        validUntil=certifiedAt.plusDays(VALID_PERIOD_DAYS);
    }
}
