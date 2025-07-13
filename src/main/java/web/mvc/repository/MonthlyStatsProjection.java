package web.mvc.repository;

import java.math.BigDecimal;

/**
 * 월별 통계
 * - JQPL new dto 쿼리문의 타입 불일치 해결
 * */
public interface MonthlyStatsProjection {
    Integer getYear();
    Integer getMonth();
    Long    getReservationCount();
    Long    getRevenue();
}
