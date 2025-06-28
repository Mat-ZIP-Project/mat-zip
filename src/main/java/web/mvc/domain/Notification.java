//package web.mvc.domain;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@NoArgsConstructor
//@AllArgsConstructor
//@Getter
//@Setter
//@Builder
//@Table(name = "notifications")
//public class Notification {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "notification_id")
//    private Long notificationId;
//
//    @Column(nullable = false)
//    private String title;
//
//    @Column(nullable = false)
//    private String body;
//
//    @Column(name = "created_at", nullable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name = "is_read", columnDefinition = "BOOLEAN DEFAULT FALSE")
//    private Boolean isRead;
//
//    @ManyToOne
//    @JoinColumn(name = "reservationId", nullable = false)
//    private Reservation reservation;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "id", nullable = false)
//    private User user;
//
//    @PrePersist
//    protected void onCreate() {
//        this.createdAt = LocalDateTime.now();
//        if (this.isRead == null) {
//            this.isRead = false;
//        }
//    }
//}
