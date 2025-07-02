package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "meetup_participants")
public class MeetupParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "join_id")
    private Long joinId;

    @Column(name = "join_status", nullable = false)
    private String joinStatus; // APPLIED, APPROVED 등

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @PrePersist
    public void onCreate() {
        this.joinedAt = LocalDateTime.now();
        if(this.joinStatus == null) this.joinStatus = "APPLIED";
        //회원이 언제 모임에 참가했는지 타임스탬프 자동 입력
        //기본값으로 "APPLIED"로 자동 지정. "APPLIED" → "APPROVED"(승인됨), "REJECTED"(거절됨) 등으로 상태가 바뀜.
    }
}
