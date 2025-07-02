package web.mvc.domain;

import jakarta.persistence.*;
import lombok.*;
import web.mvc.util.Enums;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="reservation_payments")
public class ReservationPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    // ManyToOne 관계: reservation_payments 테이블의 'reservation_id' 컬럼은 reservations 테이블의 'reservation_id'를 참조
    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private Integer amount; // 결제 금액

    @Column(name = "original_amount", nullable = false)
    private Integer originalAmount;

    @Column(name = "discount_amount")
    private Integer discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20) // NULL 허용 (결제 전에는 null일 수 있음)
    private Enums.PaymentStatus status; // 결제 상태 (PAID, READY, CANCELLED, FAILED 등)

    @Column(name = "paid_at")
    private LocalDateTime paidAt; // 결제 완료 시간

    // ManyToOne 관계: reservation_payments 테이블의 'id' 컬럼은 users 테이블의 'id'를 참조
    @ManyToOne
    @JoinColumn(name = "id", nullable = false) // DDL에 명시된 FK 컬럼 이름 'id'
    private User user; // 결제를 수행한 사용자

    @Column(name = "portone_imp_uid", unique = true) // PortOne의 imp_uid (결제 고유 번호)
    private String impUid;

    @Column(name = "portone_merchant_uid", unique = true) // PortOne의 merchant_uid (가맹점 주문 번호)
    private String merchantUid;


}
