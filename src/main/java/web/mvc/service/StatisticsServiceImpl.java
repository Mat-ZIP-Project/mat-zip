package web.mvc.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import web.mvc.dto.DailyStatsDto;
import web.mvc.dto.MonthlyStatsDto;
import web.mvc.dto.ReviewSummaryDto;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.ReservationRepository;
import web.mvc.repository.RestaurantRepository;
import web.mvc.repository.ReviewRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final ReservationRepository reservationRepo;
    private final ReviewRepository     reviewRepo;

    @Override
    public List<DailyStatsDto> getDailyStatsByRestaurantId(Long restaurantId, String from, String to) {
        return reservationRepo
                .findDailyStatsByRestaurantId(restaurantId, from, to)
                .stream()
                .map(p -> new DailyStatsDto(
                        p.getDate(),
                        p.getReservationCount(),
                        p.getRevenue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyStatsDto> getMonthlyStatsByRestaurantId(Long restaurantId, String from, String to) {
        return reservationRepo
                .findMonthlyStatsByRestaurantId(restaurantId, from, to)
                .stream()
                .map(p -> new MonthlyStatsDto(
                        p.getYear(),
                        p.getMonth(),
                        p.getReservationCount(),
                        p.getRevenue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ReviewSummaryDto getReviewSummary(Long restaurantId, LocalDate from, LocalDate to) {
        return reviewRepo.findReviewSummaryByVisitDate(restaurantId, from, to);
    }
}
