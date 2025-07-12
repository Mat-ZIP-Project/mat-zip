package web.mvc.dto;

import lombok.*;

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
