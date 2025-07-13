package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.DailyStatsDto;
import web.mvc.dto.MonthlyStatsDto;
import web.mvc.dto.ReviewSummaryDto;
import web.mvc.service.StatisticsService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/owner/statistics/{restaurantId}")
public class OwnerStatisticsController {
    private final StatisticsService statsService;

    /** 일별 통계 */
    @GetMapping("/reservations/daily")
    public List<DailyStatsDto> dailyStats( @PathVariable Long restaurantId,
                                           @RequestParam String from,
                                           @RequestParam String to) {
        return statsService.getDailyStatsByRestaurantId(restaurantId, from, to);
    }

    /** 월별 통계 */
    @GetMapping("/reservations/monthly")
    public List<MonthlyStatsDto> monthlyStats(@PathVariable Long restaurantId,
                                              @RequestParam String from,
                                              @RequestParam String to) {
        return statsService.getMonthlyStatsByRestaurantId(restaurantId, from, to);
    }

    /** 리뷰 통계 (visitDate 기준) */
    @GetMapping("/reviews/summary")
    public ReviewSummaryDto reviewSummary(@PathVariable Long restaurantId,
              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return statsService.getReviewSummary(restaurantId, from, to);
    }
}