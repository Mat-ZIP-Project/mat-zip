package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.ReviewReportDto;
import web.mvc.service.ReviewReportService;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewReportController {

    private final ReviewReportService reviewReportService;

    @PostMapping("/report")
    public String reportReview(@RequestBody ReviewReportDto dto) {
        return reviewReportService.reportReview(dto);
    }
}
