package web.mvc;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import web.mvc.security.JwtTokenProvider;

//@SpringBootTest
@Slf4j
class JwtTokenTests {

//    @Autowired
//    private JwtTokenProvider jwtTokenProvider;
//
//    /**
//     *  Registed claims : 미리 정의된 클레임
//     *      iss(issuer: 발행자),
//     * 		exp(expireation time: 만료 시간),
//     * 		sub(subject: 제목),
//     * 		iat(issued At: 발행 시간),
//     * 		jti(JWI ID: 토큰의 고유 식별자)
//     *
//     * 	Registed claims : 미리 정의된 클레임
//     * */
//    @Test
//    void tokenCreateTest() {
//
//        String token = jwtTokenProvider.createAccessToken("jang", "ROLE_USER");
//        String refreshToken = jwtTokenProvider.createRefreshToken("jang");
//        log.info("token = " + token);
//        log.info("refreshToken = " + refreshToken);
//
//        System.out.println("---조회하기 (검증)---");
//        System.out.println("userId = " + jwtTokenProvider.getUserId(token) + " | "+ jwtTokenProvider.getUserId(refreshToken));
//        System.out.println("Role = " + jwtTokenProvider.getRole(token) +" | " + jwtTokenProvider.getRole(refreshToken));
//        System.out.println("validateToken = " + jwtTokenProvider.validateToken(token) +" | "+ jwtTokenProvider.validateToken(refreshToken));
//    }
}