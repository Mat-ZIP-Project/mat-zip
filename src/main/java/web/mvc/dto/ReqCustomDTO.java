package web.mvc.dto;

import lombok.*;
import web.mvc.domain.CustomCourse;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ReqCustomDTO {
    private Long restaurantId;
    private String restaurantName;
    private int visitOrder;
    private CustomCourse customCourse;
}
