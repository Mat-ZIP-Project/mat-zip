package web.mvc.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/*
 JWT 정보 검증 및 생성
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private SecretKey secretKey;//Decode한 secret key를 담는 객체
    private final long ACCESS_TOKEN_VALIDITY = 1000L * 60 * 30;  // 10분
    private final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 14; // 14일

    //application.properties에 있는 미리 Base64로 Encode된 Secret key를 가져온다
    public JwtTokenProvider(@Value("${spring.jwt.secret}") String secret) {
        log.info("JWTUtil 생성자 .... = {}" , secret);
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    //Bearer : JWT에 대한 토큰 사용
    //claim은 payload에 해당하는 정보
    public String createAccessToken(String userId, String role) {
        log.info("토큰 생성 - userId: {}, role: {}", userId, role);
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis())) //현재로그인된 시간
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY)) //만료시간
                .signWith(secretKey)
                .compact(); //tonken을 하나의 문자열로 리턴
    }

    //refresh 토큰 생성
    public String createRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .signWith(secretKey)
                .compact();
    }

    //토큰 유효성체크 (서명·만료·포맷 검증)
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build()
                    .parseClaimsJws(token);
            log.info("토큰 검증 성공");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    //검증 Id
    public String getUserId(String token) {
        return Jwts.parser()
                //.setSigningKey(secretKey)
                .verifyWith(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

    }

    //검증 Role
    public String getRole(String token) {
        String role = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload().get("role", String.class);
        log.info("토큰에서 추출된 role: {}", role);
        return role;
    }

}
