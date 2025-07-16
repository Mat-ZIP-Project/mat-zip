package web.mvc.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.siot.IamportRestClient.IamportClient;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @PersistenceContext
    private EntityManager em;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // 최적화 설정
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)  // 안전한 매핑(엄격한 매칭)
                .setFieldMatchingEnabled(true)                   // Lombok 호환(필드 매칭 활성화)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);  // private 필드 접근 허용

        return mapper;
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }


    @Value("${IAMPORT_REST_API_KEY}")
    private String apiKey;

    @Value("${IAMPORT_REST_API_SECRET}")
    private String apiSecret;

    @Bean
    public IamportClient client() {
        return new IamportClient(apiKey, apiSecret);

    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


}
