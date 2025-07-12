package web.mvc.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcrResponse {
    private List<OcrImageResult> images;
}
