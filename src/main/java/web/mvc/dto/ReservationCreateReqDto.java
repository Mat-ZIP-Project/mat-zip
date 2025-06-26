package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateReqDto {
    private String date;
    private String time;
    private Integer numPeople;
    private String restaurantName;
    private Long id;

}
