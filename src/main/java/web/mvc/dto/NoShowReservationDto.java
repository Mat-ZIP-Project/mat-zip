package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 노쇼 후보 목록 조회 응답
 * */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoShowReservationDto {
    private Long reservationId;
    private String userId;
    private String userName;
    private String reservationDate;
    private String reservationTime;
}
