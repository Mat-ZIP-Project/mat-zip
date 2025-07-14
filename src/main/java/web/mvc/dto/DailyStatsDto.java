package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@NoArgsConstructor
@AllArgsConstructor
/**
 * 식당 사장 통계차트용 (일간)
 */
public class DailyStatsDto {
    private String date;
    private Long reservationCount;
    private Long revenue;
}

