package web.mvc.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OcrRequestDto {
    private String imageBase64;
}
