package web.mvc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class OcrImage {
    private String format;
    private String name;
    private String data;
}
