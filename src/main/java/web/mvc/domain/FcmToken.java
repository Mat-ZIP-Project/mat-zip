//package web.mvc.domain;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Getter
//@Setter
//@Entity
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@Table(name = "fcm_tokens")
//public class FcmToken {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "token_id")
//    private Long fcmTokenId;
//
//    @Column(name = "device_token", nullable = false)
//    private String deviceToken;
//
//    @ManyToOne
//    @JoinColumn(name = "id", nullable = false)
//    private User user;
//}
