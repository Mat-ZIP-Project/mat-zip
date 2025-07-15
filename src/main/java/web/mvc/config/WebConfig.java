package web.mvc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//@Configuration
//@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    
    //현지인 인증 기능 테스트 위해서 front 실행 포트 허용
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")

                .allowedOrigins(
                       // "http://localhost:63342", "http://localhost:5173", "http://localhost:3000",
                      //  "http://13.209.64.215", "http://13.209.64.215:80",
                      //  "https://mat-zip.kro.kr"
                        "http://localhost:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE","OPTIONS")
                .allowCredentials(true)
                .allowedHeaders("*");
    }
}
