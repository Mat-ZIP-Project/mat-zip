package web.mvc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CoolSMS API 설정 바인딩
 * application-secret.properties의 `coolsms.api` 아래 필드 매핑
 */
@Component
@ConfigurationProperties(prefix = "coolsms.api")
@Getter @Setter
public class CoolsmsProperties {

    // CoolSMS API Key
    private String key;

    // CoolSMS API Secret
    private String secret;

    // 발신자 번호
    private String fromNumber;
}
