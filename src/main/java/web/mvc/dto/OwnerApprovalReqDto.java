package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerApprovalReqDto {
    private Long reservationId;
    private String reservationStatus;
    private String ownerNotes;
}
