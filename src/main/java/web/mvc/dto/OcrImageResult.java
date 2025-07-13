package web.mvc.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrImageResult {
    private List<OcrField> fields;
}
