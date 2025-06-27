//package web.mvc.config;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//
//import java.io.InputStream;
//
///**
// * FCM을 사용할 수 있도록 Firebase Admin SDK를 초기화하는 설정 파일.
// * 서비스 계정 키 (JSON 파일)를 로드하여 Firebase와의 연결을 설정
// */
//@Configuration
//@Slf4j
//public class FirebaseConfig {
//
//    @Bean
//    public FirebaseApp firebaseApp() throws Exception {
//
//        ClassPathResource resource = new ClassPathResource("firebase-admin.json");
//        InputStream serviceAccount = resource.getInputStream();
//
//        FirebaseOptions options = FirebaseOptions.builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .setProjectId("ttttest-a2690")
//                .build();
//
//        if (FirebaseApp.getApps().isEmpty()) {
//            FirebaseApp app = FirebaseApp.initializeApp(options);
//            log.info("Firebase app initialized");
//            return app;
//        } else {
//            log.info("Firebase app already initialized");
//            return FirebaseApp.getInstance();
//        }
//    }
//}
