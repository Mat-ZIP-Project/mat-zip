package web.mvc.config; // 실제 프로젝트의 패키지 경로에 맞게 수정하세요.

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Spring Security 활성화 어노테이션
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF (Cross-Site Request Forgery) 보호 비활성화
                // REST API 개발 및 Postman 테스트 시 편리하지만, 실제 서비스에서는 보안상 권장하지 않음.
                .csrf(csrf -> csrf.disable())

                // 2. 모든 HTTP 요청에 대해 접근을 허용 (permitAll())
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll() // 모든 요청(anyRequest)을 인증 없이 허용(permitAll)
                )

                // 3. 폼 로그인 비활성화 (선택 사항이지만 명시적으로 끄는 것이 좋습니다)
                // 기본 로그인 페이지가 뜨는 것을 방지합니다.
                .formLogin(form -> form.disable())

                // 4. HTTP Basic 인증 비활성화 (선택 사항이지만 명시적으로 끄는 것이 좋습니다)
                // 브라우저에서 팝업으로 사용자/비밀번호를 묻는 것을 방지합니다.
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }

    // 개발 목적으로 비밀번호 인코더를 설정하지 않아도 되는 경우 (로그인/회원가입 기능 미사용 시)
    // 실제 사용자 계정 기능을 구현할 때는 BCryptPasswordEncoder 등을 사용해야 합니다.
    // @Bean
    // public PasswordEncoder passwordEncoder() {
    //     return NoOpPasswordEncoder.getInstance(); // NoOpPasswordEncoder는 절대로 프로덕션에서 사용하면 안 됩니다.
    // }
}