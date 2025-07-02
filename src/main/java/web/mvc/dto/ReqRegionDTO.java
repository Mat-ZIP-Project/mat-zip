package web.mvc.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ReqRegionDTO {
    private String regionSido;
    private String regionSigungu;
}
