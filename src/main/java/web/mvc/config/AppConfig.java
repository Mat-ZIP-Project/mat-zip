package web.mvc.config;

import com.siot.IamportRestClient.IamportClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Value("${iamport.rest-api-key}")
    private String apiKey;

    @Value("${iamport.rest-api-secret}")
    private String apiSecret;

    @Bean
    public IamportClient client() {
        return new IamportClient(apiKey, apiSecret);
    }

}
