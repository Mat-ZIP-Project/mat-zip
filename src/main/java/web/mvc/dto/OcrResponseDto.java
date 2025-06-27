package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OcrResponseDto {
    private String version;
    private String requestId;
    private long timestamp;
    private List<Image> images;

    @Getter
    @Setter
    public static class Image {
        private String name;
        private String inferResult;
        private List<Field> fields;

        @Getter
        @Setter
        public static class Field {
            private String name;
            private String inferText;
        }
    }
}
