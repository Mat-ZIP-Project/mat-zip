package web.mvc.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OcrRequest {
    private String version;
    private String requestId;
    private long timestamp;
    private List<OcrImage> images;
}
