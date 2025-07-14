package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
/**
 * 식당 사장 통계차트용 (월간)
 */
public class MonthlyStatsDto {
    private Integer year;   // primitive -> wrapper
    private Integer month;
    private Long reservationCount;
    private Long revenue;
}
