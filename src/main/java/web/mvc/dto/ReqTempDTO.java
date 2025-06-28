package web.mvc.dto;

import lombok.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqTempDTO {
    private Long userId; //user테이블의 pk
    private Long restaurantId;
    private String restaurantName;
    private int visitOrder;
}
