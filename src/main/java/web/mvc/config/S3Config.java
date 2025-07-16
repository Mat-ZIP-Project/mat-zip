package web.mvc.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AWS S3 클라이언트 설정을 위한 Configuration 클래스
 * - 로컬 개발 및 GitHub Actions, EC2 환경 등 다양한 환경에서 작동하도록
 *   DefaultAWSCredentialsProviderChain을 사용하여 환경변수, IAM Role 등을 우선순위로 자동 인증
 */
@Configuration
public class S3Config {

    @Value("${aws.credentials.accessKey}")
    private String accessKey;

    @Value("${aws.credentials.secretKey}")
    private String secretKey;

    @Value("${aws.region.static}")
    private String region;

    @Bean
    public AmazonS3 amazonS3() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withRegion(region);

        // 명시적 키가 설정된 경우 해당 자격증명 사용
        if (!accessKey.isEmpty() && !secretKey.isEmpty()) {
            BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
            builder.withCredentials(new AWSStaticCredentialsProvider(creds));
        } else {
            // 설정 없는 경우 DefaultAWSCredentialsProviderChain 사용
            builder.withCredentials(new DefaultAWSCredentialsProviderChain());
        }

        return builder.build();
    }
}
