package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 대기중인 예약 목록 조회 응답
 * */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingReservationDto {
    private Long reservationId;
    private String userId;
    private String userName;
    private boolean noShow;
    private int numPeople;
    private String date;  // YYYY-MM-DD
    private String time;  // HH:mm
    private String status;
}