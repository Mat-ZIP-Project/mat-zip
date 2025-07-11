package web.mvc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import web.mvc.domain.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

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
        log.info("JWTFilter - doFilter메소드 호출");

        // *** 요청 상세 정보 로깅 *** (디버깅용)
        log.info("요청 URI: {}, Method: {}, Content-Type: {}",
                request.getRequestURI(), request.getMethod(), request.getContentType());

        // Authorization 헤더 확인
        String header = request.getHeader("Authorization");

        log.info("Authorization Header: {}", header);

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer 제거 후 토큰 추출
        String token = header.substring(7);

        // 토큰 유효성 검사 (서명·만료 확인)
        if (!jwtTokenProvider.validateToken(token)) {
            log.info("유효하지 않은 토큰: {}", token);
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰에서 사용자 정보 추출
        String userId = jwtTokenProvider.getUserId(token);
        String role = jwtTokenProvider.getRole(token);

        log.info("[JWTFilter] 토큰에서 추출된 사용자: {}, 권한: {}", userId, role);

        // 권한 설정
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));

        // UserDetails 조회
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(userId);

        // SecurityContext에 인증 정보 저장
        Authentication authToken = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.info("SecurityContext에 인증 정보 저장 완료. 권한: {}", authorities);

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        log.info("현재 인증사용자 ID: {}, 권한: {}",
                currentAuth != null ? currentAuth.getName() : "null",
                currentAuth != null ? currentAuth.getAuthorities() : "null");

        filterChain.doFilter(request, response);
    }
}
