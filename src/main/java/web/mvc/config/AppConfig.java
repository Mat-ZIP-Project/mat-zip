package web.mvc.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
//import com.siot.IamportRestClient.IamportClient;

import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @PersistenceContext
    private EntityManager em;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }

//    @Value("${iamport.rest-api-key}")
//    private String apiKey;
//
//    @Value("${iamport.rest-api-secret}")
//    private String apiSecret;
//
//    @Bean
//    public IamportClient client() {
//        return new IamportClient(apiKey, apiSecret);
//
//    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


}
