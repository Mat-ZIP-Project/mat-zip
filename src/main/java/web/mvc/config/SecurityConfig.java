package web.mvc.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import web.mvc.security.*;
import web.mvc.service.TokenService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        log.info("bCryptPasswordEncoder call.....");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        log.info("authenticationManager --- {}", configuration);
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // ✅ CORS 설정 적용
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.csrf(csrf -> csrf.disable())
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable());

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
        );

        http.authorizeHttpRequests(auth -> auth
                // ✅ Preflight OPTIONS 요청은 모두 허용
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 인증 필요한 요청
                .requestMatchers("/auth/logout").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/restaurants/like/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/restaurants/like/**").authenticated()

                // 접근 허용 요청
                .requestMatchers("/login", "/auth/**", "/signup/**",
                        "/payment/complete", "/map/**", "/api/reviews/**",
                        "/api/restaurants/**", "/api/waiting/status/**", "/test").permitAll()

                // 권한별 접근 제한
                .requestMatchers("/owner/**", "/reservation/owner/approve").hasRole("OWNER")
                .requestMatchers(HttpMethod.PUT, "/api/waiting/next/**", "/api/waiting/noshow/**").hasRole("OWNER")
                .requestMatchers(HttpMethod.GET, "/api/waiting/owner/me").hasRole("OWNER")
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
        );

        // JWT 인증 필터 등록
        http.addFilterAt(new LoginFilter(
                        this.authenticationManager(authenticationConfiguration),
                        jwtTokenProvider,
                        tokenService),
                UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(new JWTFilter(jwtTokenProvider, userDetailsService),
                LoginFilter.class);

        return http.build();
    }

    /**
     * ✅ 정확한 CORS 설정을 위한 CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Origin은 패턴 기반으로 유연하게
        configuration.setAllowedOriginPatterns(List.of("https://mat-zip.kro.kr", "http://localhost:5173"));

        // ✅ HTTP 메서드는 반드시 명시 (와일드카드 "*” 는 무시됨)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // ✅ 요청 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // ✅ 프론트에서 Authorization 헤더를 응답에서 확인할 수 있도록 노출
        configuration.setExposedHeaders(List.of("Authorization"));

        // ✅ 인증 정보 포함 허용
        configuration.setAllowCredentials(true);

        // ✅ 캐시 시간
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

//package web.mvc.config;
//
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import web.mvc.security.*;
//import web.mvc.service.TokenService;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//@Slf4j
//public class SecurityConfig {
//
//    private final AuthenticationConfiguration authenticationConfiguration;
//    private final JwtTokenProvider jwtTokenProvider;
//    private final CustomUserDetailsService userDetailsService;
//    private final TokenService tokenService;
//    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
//
//    @Bean
//    public BCryptPasswordEncoder bCryptPasswordEncoder() {
//        log.info("bCryptPasswordEncoder call.....");
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
//        log.info("authenticationManager --- {}", configuration);
//        return configuration.getAuthenticationManager();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        System.out.println("********************ok******");
//        // CORS 설정
//        //http.cors(cors -> cors.configurationSource(corsConfig()));
//
//        //CORS 설정
//        http.cors((corsCustomizer ->
//                corsCustomizer.configurationSource(new CorsConfigurationSource()
//                {
//                    @Override
//                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
//                        CorsConfiguration configuration = new CorsConfiguration();
//                        //configuration.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));
//                        //configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:4173"));
//                        //configuration.setAllowedOrigins(Arrays.asList("http://52.79.227.209", "http://52.79.227.209:80"));
//                        //configuration.setAllowedOriginPatterns(Arrays.asList("https://mat-zip.kro.kr", "http://mat-zip.kro.kr"));
//                        configuration.setAllowedOrigins(List.of("https://mat-zip.kro.kr"));
//                      // configuration.setAllowedOrigins(List.of("http://13.209.64.215"));
//                        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//                        configuration.setAllowCredentials(true);
//                        configuration.setAllowedHeaders(List.of("*"));
//                        //configuration.setAllowedHeaders(Collections.singletonList("*"));
//                        configuration.setMaxAge(3600L);
//
//                        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
//
//
//
//                        return configuration;
//                    }
//                })));
//
//
//        // 기본 설정 비활성화 (JWT, 커스텀 LoginFilter 사용)
//        http.csrf(csrf -> csrf.disable())
//                .httpBasic(b -> b.disable())
//                .formLogin(f -> f.disable());
//
//        // 세션 관리 설정 (STATELESS)
//        http.sessionManagement(session ->
//                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//        );
//
//        // 예외 처리
//        http.exceptionHandling(exceptions -> exceptions
//                .authenticationEntryPoint(jwtAuthenticationEntryPoint) //401
//                .accessDeniedHandler(jwtAccessDeniedHandler)           //403
//        );
//
//        // 경로별 접근 제어
//        http.authorizeHttpRequests(auth ->
//                auth
//                        // CORS Preflight 요청(OPTIONS) 모든 경로 허용
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//
//                        // 인증 필요 (하단에 인증 허용 url 접근 전 인증 요청필요한 것만 기입)
//                        .requestMatchers("/auth/logout").authenticated()
//
//                        // 찜하기 관련 POST, DELETE 요청은 인증 필요
//                        .requestMatchers(HttpMethod.POST, "/api/restaurants/like/**").authenticated()
//                        .requestMatchers(HttpMethod.DELETE, "/api/restaurants/like/**").authenticated()
//
//                        // 접근 허용 (접근 허용 url은 무조건 명시)
//                        .requestMatchers("/login","/auth/**", "/signup/**",
//                                "/payment/complete", "/map/**", "/api/reviews/**", "/api/restaurants/**", "/api/waiting/status/**","/test").permitAll()
//
//                        // 웨이팅 사용자용
////                        .requestMatchers(HttpMethod.POST, "/api/waiting").hasRole("USER")
////                        .requestMatchers(HttpMethod.GET, "/api/waiting/me").hasRole("USER")
////                        .requestMatchers(HttpMethod.PUT, "/api/waiting/enter/**").hasRole("USER")
////                        .requestMatchers("/api/waiting/subscribe").authenticated()
//
//                        // 권한별 접근 제한
//                        .requestMatchers("/owner/**","/reservation/owner/approve").hasRole("OWNER")
//                        // 웨이팅 식당 주인용
//                        .requestMatchers(HttpMethod.PUT, "/api/waiting/next/**", "/api/waiting/noshow/**").hasRole("OWNER")
//                        .requestMatchers(HttpMethod.GET, "/api/waiting/owner/me").hasRole("OWNER")
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//
//                        // 그 외 모든 요청은 인증 필요
//                        .anyRequest().authenticated()
//        );
//
//        http.addFilterAt(
//                new LoginFilter(
//                        this.authenticationManager(authenticationConfiguration),
//                        jwtTokenProvider, tokenService),
//                UsernamePasswordAuthenticationFilter.class);
//
//        http.addFilterBefore(new JWTFilter(jwtTokenProvider, userDetailsService),
//                LoginFilter.class);
//
//        return http.build();
//    }
//
//    // CORS 설정 메서드
////    //@Bean
////    public CorsConfigurationSource corsConfig() {
////        return new CorsConfigurationSource() {
////            @Override
////            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
////                CorsConfiguration configuration = new CorsConfiguration();
////                configuration.setAllowedOrigins(
////                        Arrays.asList(
////                                //"http://localhost:5173", "http://localhost:4173",
////                               // "http://13.209.64.215", "http://13.209.64.215:80",
////                                "https://mat-zip.kro.kr", "http://mat-zip.kro.kr")
////                );
////                configuration.setAllowedMethods(Collections.singletonList("*"));
////                configuration.setAllowedHeaders(Collections.singletonList("*"));
////                configuration.setAllowCredentials(true);
////                configuration.setMaxAge(3600L);
////                configuration.setExposedHeaders(Collections.singletonList("Authorization"));
////                return configuration;
////            }
////        };
////    }
//
//
//
//
//}