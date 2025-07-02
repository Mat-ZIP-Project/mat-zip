package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.mvc.domain.SmsVerification;

import java.util.Optional;

public interface SmsVerificationRepository extends JpaRepository<SmsVerification, Long> {

    /** 가장 최근 생성된 휴대폰 인증 내역 조회 */
    @Query(value = "SELECT * FROM sms_verifications WHERE phone = :phone AND purpose = :purpose ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Optional<SmsVerification> findLatestVerification(@Param("phone") String phone, @Param("purpose") String purpose);

    /**
     * id에 해당하는 데이터 인증 완료 처리
     * @return 업데이트된 행(row) 수
     */
    @Modifying
    @Query("UPDATE SmsVerification s SET s.verified = true WHERE s.smsId = :id")
    int verifySmsById(@Param("id") Long id);

    /** 인증 여부 확인 - 회원가입 진행시 필요 */
    @Query("SELECT s FROM SmsVerification s WHERE s.phone = :phone AND s.purpose = :purpose AND s.verified = true ORDER BY s.createdAt DESC LIMIT 1")
    Optional<SmsVerification> findLatestVerifiedSms(@Param("phone") String phone, @Param("purpose") String purpose);
}
