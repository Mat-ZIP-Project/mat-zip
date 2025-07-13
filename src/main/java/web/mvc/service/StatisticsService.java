package web.mvc.service;

import web.mvc.dto.DailyStatsDto;
import web.mvc.dto.MonthlyStatsDto;
import web.mvc.dto.ReviewSummaryDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {
    /** 식당Id 기준 일별 통계 (예약건수, 매출액) */
    List<DailyStatsDto> getDailyStatsByRestaurantId(Long restaurantId, String from, String to);
    /** 식당Id 기준 월별 통계 (예약건수, 매출액) */
    List<MonthlyStatsDto> getMonthlyStatsByRestaurantId(Long restaurantId, String from, String to);
    /** 식당Id 기준 리뷰 통계 (일반리뷰, 현지인리뷰 수) */
    ReviewSummaryDto getReviewSummary(Long restaurantId, LocalDate from, LocalDate to);
}
