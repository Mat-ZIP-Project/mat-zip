package web.mvc.security;

import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import web.mvc.domain.User;
import web.mvc.dto.LoginRequest;
import web.mvc.dto.TokenResponse;
import web.mvc.repository.RefreshTokenRepository;
import web.mvc.service.TokenService;
import web.mvc.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter{ //폼값 받는 컨트롤러 역할의 필터
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final Gson gson = new Gson();

    public LoginFilter(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenService = tokenService;
    }

    /** 로그인 시도 */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException{
        log.info("LoginFilter - 로그인 요청");
        String username = null;
        String password = null;

        try {
            // Content-Type 확인
            String contentType = request.getContentType();

            if (contentType != null && contentType.contains("application/json")) {
                // JSON 요청 처리
                StringBuilder json = new StringBuilder();
                String line;
                try (BufferedReader reader = request.getReader()) {
                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                    }
                }

                LoginRequest loginRequest = gson.fromJson(json.toString(), LoginRequest.class);
                username = loginRequest.getUserId();
                password = loginRequest.getPassword();

            } else {
                // Form 데이터 처리 (클라이언트 로그인 요청시 id, password 받아서 출력)
                username = super.obtainUsername(request);
                password = super.obtainPassword(request);
            }

        } catch (IOException e) {
            log.error("로그인 요청 파싱 오류", e);
            throw new RuntimeException("로그인 요청을 처리할 수 없습니다.", e);
        }

        log.info("username={}",username);
        log.info("password={}",password);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password, null);

        return authenticationManager.authenticate(authToken);
    }

    /** 로그인 성공시 실행하는 메소드 (JWT를 발급) */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication) throws  IOException{
        response.setContentType("application/json;charset=UTF-8");
        log.info("LoginFilter - 로그인 성공");

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = customUserDetails.getUser();

        // 사용자 권한 추출
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority(); //ROLE_USER or ROLE_ADMIN 첫 번째 권한만 사용

        log.info("사용자 권한: {}", role);

        // 토큰 생성 및 저장
        TokenResponse tokenResponse = tokenService.generateTokens(user);

        // RefreshToken을 HttpOnly 쿠키로 설정
        Cookie refreshCookie = new Cookie("refreshToken", tokenResponse.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // 배포환경에서 true (HTTPS)
        refreshCookie.setMaxAge(60 * 60 * 24 * 14); // 14일
        refreshCookie.setPath("/");
        refreshCookie.setAttribute("SameSite", "Lax"); //크로스사이트 POST에도 쿠키가 전송
        response.addCookie(refreshCookie);

        // 응답 Body에는 AccessToken만 포함
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("accessToken", tokenResponse.getAccessToken());
        responseData.put("user", Map.of(
                "id", user.getId(),
                "userId", user.getUserId(),
                "name", user.getName(),
                "role", role
        ));

        // 응답할 헤더를 설정 (베어러 뒤에 공백 - 관례적인  prefix)
        //response.addHeader("Authorization", "Bearer " + tokenResponse.getAccessToken());

        response.getWriter().print(gson.toJson(responseData));
    }

    /** 로그인 실패 */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        log.info("로그인 실패");

        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("errMsg", "정보를 다시 확인해주세요.");

        response.getWriter().print(gson.toJson(map));
    }
}
