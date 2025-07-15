package web.mvc;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import web.mvc.domain.User;
import web.mvc.repository.UserRepository;

//@SpringBootTest
@Slf4j
public class SecurityTest {
//    @Autowired
//    private PasswordEncoder passwordEncoder; //BCrypt가 아닌 passwordEncoder(인터페이스)를 주입받음
//    @Autowired
//    private UserRepository userRepository;
//
//    /**
//     * 패스워드 암호화 테스트
//     * */
//    @Test
//    @DisplayName("암호화 test")
//    void passwordTest() {
//        String rawPassword = "12345";
//
//        //비밀번호 인코딩
//        String encodedPassword = passwordEncoder.encode(rawPassword); //평문 -> 암호화
//        log.info("encoded password: {}", encodedPassword);
//
//        //비밀번호 매칭 확인 (평문과 암호화된 비번 같은지 매칭)
//        boolean isPasswordMatch = passwordEncoder.matches(rawPassword, encodedPassword);
//        log.info("isPasswordMatch: {}", isPasswordMatch);
//    }
//
//    /**
//     * 사용자 등록
//     * */
//    @Test
//    @DisplayName("사용자 계정추가")
//    void insertSampleUsers() {
//        userRepository.save(
//                User.builder()
//                        .userId("user5")
//                        .password(passwordEncoder.encode("1234")) //비번 암호화
//                        .name("홍길동")
//                        .phone("010-5555-5678")
//                        .role("ROLE_USER")
//                        .userStatus("활성")
//                        .pointBalance(0)
//                        .noShow(false)
//                        .gpsVerified(false)
//                        .userGrade("먹짱")
//                        .termsAgreed(true)
//                        .privacyAgreed(true)
//                        .build());
//    }
//
//
//    /**
//     * 관리자 등록
//     * */
//    @Test
//    @DisplayName("관리자 계정추가")
//    void memberInsert() {
//        String encPwd = passwordEncoder.encode("1234"); //비번 암호화
//
//        userRepository.save(
//                User.builder()
//                        .userId("admin")
//                        .password(encPwd)
//                        .name("관리자")
//                        .phone("010-1111-2222")
//                        .role("ROLE_ADMIN")
//                        .userStatus("활성")
//                        .pointBalance(0)
//                        .noShow(false)
//                        .gpsVerified(false)
//                        .privacyAgreed(true)
//                        .termsAgreed(true)
//                        .userGrade("새싹")
//                        .build());
//    }
//
//    /**
//     * 관리자 등록
//     * */
//    @Test
//    @DisplayName("관리자 계정추가")
//    void insertSampleOwner() {
//        String encPwd = passwordEncoder.encode("1234"); //비번 암호화
//
//        userRepository.save(
//                User.builder()
//                        .userId("owner")
//                        .password(encPwd)
//                        .name("사장")
//                        .phone("010-1441-2222")
//                        .role("ROLE_OWNER")
//                        .userStatus("활성")
//                        .pointBalance(0)
//                        .noShow(false)
//                        .gpsVerified(false)
//                        .privacyAgreed(true)
//                        .termsAgreed(true)
//                        .userGrade("새싹")
//                        .build());
//    }

}
