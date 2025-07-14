package web.mvc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrPaymentInfo {
    private OcrDate date;
}
