package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.ReviewReport;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);
}
