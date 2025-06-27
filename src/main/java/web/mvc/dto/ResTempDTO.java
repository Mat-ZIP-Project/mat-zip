package web.mvc.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResTempDTO {
    private Long restaurantId;
    private String restaurantName;
    private int visitOrder;


    private double latitude;
    private double longitude;

}
