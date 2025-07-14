package web.mvc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcrResponse {
    private String version;
    private String requestId;       // 추가
    private Long timestamp;
    private List<OcrImageResult> images;
}
