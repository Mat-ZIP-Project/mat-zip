package web.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreparationResDto {

    private Long reservationId;
    private String merchantUid;
    private Integer amount;
    private String message;
    private boolean success;

}
