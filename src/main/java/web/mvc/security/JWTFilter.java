package web.mvc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import web.mvc.domain.User;

import java.io.IOException;

/** 모든 요청의 JWT 토큰 검증 */
@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        //request에서 Authorization 헤더를 찾음
        String header = request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("Authorization 헤더 없음 or Bearer 미포함");
            filterChain.doFilter(request, response); //다음 필터를 호출...

            //조건이 해당되면 메소드 종료
            return;
        }

        //Bearer 부분 제거 후 순수 토큰만 획득
        String token = header.substring(7);
        // 토큰 유효성 검사 (서명·만료 확인)
        if (!jwtTokenProvider.validateToken(token)) {
            log.info("유효하지 않은 토큰: {}", token);
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰(클레임)에서 사용자 정보 추출
        String userId = jwtTokenProvider.getUserId(token);
        String role   = jwtTokenProvider.getRole(token);

        // User 엔티티 생성하여 값 set
//        User user = new User();
//        user.setUserId(userId);
//        user.setRole(role);

        //UserDetails에 회원 정보 객체 담기
        //CustomUserDetails userDetails = new CustomUserDetails(user);
        CustomUserDetails userDetails = (CustomUserDetails)
                userDetailsService.loadUserByUsername(userId);

        //Authentication 객체 생성 및 SecurityContext에 저장
        Authentication authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken); //저장

        filterChain.doFilter(request, response);//다음 필터를 호출
    }
}
