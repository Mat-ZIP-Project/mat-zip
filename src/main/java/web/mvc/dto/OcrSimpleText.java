package web.mvc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcrSimpleText {
    private String text;
}
