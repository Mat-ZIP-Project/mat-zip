package web.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import web.mvc.domain.ReviewReport;
import web.mvc.dto.ReviewReportDto;
import web.mvc.repository.ReviewReportRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewReportService {

    private final ReviewReportRepository reviewReportRepository;

    public String reportReview(ReviewReportDto dto) {
        boolean alreadyReported = reviewReportRepository.existsByUserIdAndReviewId(dto.getUserId(), dto.getReviewId());

        if (alreadyReported) {
            return "이미 신고한 리뷰입니다.";
        }

        ReviewReport report = new ReviewReport();
        report.setUserId(dto.getUserId());
        report.setReviewId(dto.getReviewId());
        report.setReason(dto.getReason());
        report.setReportedAt(LocalDateTime.now());

        reviewReportRepository.save(report);
        return "리뷰를 신고했습니다.";
    }
}
