package web.mvc.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResCustomDTO {
    private Long courseId;
    private String title;

    private List<ResTempDTO> resTempDTOList;
}
