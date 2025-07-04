package web.mvc.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import web.mvc.security.*;
import web.mvc.service.TokenService;

import java.util.Arrays;

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

    /** 비밀번호 암호화 */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        log.info("bCryptPasswordEncoder call.....");
        return new BCryptPasswordEncoder();
    }

    /** AuthenticationManager Bean 등록 */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        log.info("authenticationManager --- {}", configuration);
        return configuration.getAuthenticationManager();
    }

    /**
     * SecurityFilterChain - security 정책
     * */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // csrf, formLogin, httpBasic 비활성화
        http.csrf((auth)-> auth.disable());
        http.formLogin((auth)-> auth.disable());
        http.httpBasic((auth)-> auth.disable());

        http    //401, 403에러 처리
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                .authorizeHttpRequests((auth) ->
                auth
                        // 인증 필요
                        .requestMatchers("/auth/logout").authenticated()

                        // 접근 허용
                        .requestMatchers("/auth/**", "/api/reviews/**", "/signup/**",
                                "/api/v1/fcm/registerToken", "/api/payment/complete", "/map/**", "/api/**").permitAll()

                        // 권한별 접근제한
                        .requestMatchers("/owner/**").hasRole("OWNER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated());


        http.addFilterAt(
                new LoginFilter(
                        this.authenticationManager(authenticationConfiguration),
                        jwtTokenProvider, tokenService),
                UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(new JWTFilter(jwtTokenProvider, userDetailsService),
                LoginFilter.class);

        return http.build();
    }
}
