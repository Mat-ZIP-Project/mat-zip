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
import web.mvc.security.CustomUserDetailsService;
import web.mvc.security.JWTFilter;
import web.mvc.security.JwtTokenProvider;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() { //비밀번호 암호화 담당
        log.info("bCryptPasswordEncoder call.....");
        return new BCryptPasswordEncoder();
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        log.info("authenticationManager ---= {}", configuration);
        return configuration.getAuthenticationManager();
    }

    /**
     * SecurityFilterChain - security 정책
     * */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //csrf 비활성화
        http.csrf((auth)-> auth.disable());

        //form 로그인 방식 disable -> React, JWT 인증 방식으로 변경예정
        //UsernamePasswordAuthenticationFilter 비활성
        http.formLogin((auth)-> auth.disable());

        //http basic 인증 방식 disable
        http.httpBasic((auth)-> auth.disable());

        http.authorizeHttpRequests((auth) ->
                auth
                        .requestMatchers("/login", "/refresh").permitAll() //컨트롤러로 바로 접근, path 추가하기
                /* swagger API  .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll() */
                        // [1] GET 요청: 누구나 접근 가능
                        .requestMatchers(HttpMethod.GET, "/user").permitAll()

                        // [2] POST 요청: 인증 필요
                        .requestMatchers(HttpMethod.POST, "/user").authenticated()

                        // [3] PUT 요청: 인증 필요
                        // .requestMatchers(HttpMethod.PUT, "/user/**").authenticated()

                        // [4] DELETE 요청: 인증 필요
                        // .requestMatchers(HttpMethod.DELETE, "/user/**").authenticated()

                        .requestMatchers("/admin").hasRole("ADMIN") //ROLE_ADMIN만 접근 가능
                        .anyRequest().authenticated());

        http.addFilterBefore(new JWTFilter(jwtTokenProvider, userDetailsService),
                UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
}