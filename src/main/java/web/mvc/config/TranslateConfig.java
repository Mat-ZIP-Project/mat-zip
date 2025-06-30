package web.mvc.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class TranslateConfig {

    @Bean
    public Translate translate() throws IOException {
        FileInputStream serviceAccountStream =
                new FileInputStream("src/main/resources/matzip-google-key.json");

        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);
        return TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
    }
}
