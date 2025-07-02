package web.mvc.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ReqPositionDTO {
    private double latitude;
    private double longitude;
    private long radius;  //m 단위
}
