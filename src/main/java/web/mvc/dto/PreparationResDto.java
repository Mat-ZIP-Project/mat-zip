package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PreparationResDto {

    private Long reservationId;
    private String merchantUid;
    private Integer originalAmount;
    private Integer discountAmount;
    private Integer finalPaymentAmount;
    private String message;
    private boolean success;

}
