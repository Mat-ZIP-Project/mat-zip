package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "meetings")
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Long meetingId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "current_participants")
    private Integer currentParticipants;

    @Column(name = "meeting_time", nullable = false)
    private LocalDateTime meetingTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 주최자(회원) 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private User user;

    // 연관 식당
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // 모임에서 참가자 목록을 자주 조회해야 할 때(양방향 필요 시)
    // @OneToMany(mappedBy = "meeting")
    // private List<MeetupParticipant> participants;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if(this.currentParticipants == null) this.currentParticipants = 1; // 새로운 모임을 생성하면 개설자 본인이 참가자 1명으로 카운트.
    }

}
