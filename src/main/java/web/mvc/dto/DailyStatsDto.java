package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * 식당 사장 통계차트용
 */
public class DailyStatsDto {
    private LocalDate date;
    private Long reservationCount;
    private BigDecimal revenue;
}
