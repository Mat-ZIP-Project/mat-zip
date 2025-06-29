package web.mvc.service;

public interface SmsVerificationService {
    /** SMS 인증코드 발송 */
    void sendVerificationCode(String phone, String purpose);

    /** SMS 인증코드 검증 */
    void verifyCode(String phone, String code, String purpose);
}
