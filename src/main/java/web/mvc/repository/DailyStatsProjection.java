package web.mvc.repository;

/**
 * 일별 통계
 * - JQPL new dto 쿼리문의 타입 불일치 해결
 * */
public interface DailyStatsProjection {
    String getDate();
    Long   getReservationCount();
    Long   getRevenue();
}
