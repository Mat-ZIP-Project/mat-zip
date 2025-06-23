package web.mvc.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@RequiredArgsConstructor
@Getter
@Setter
public class ResBadgeDTO {
    private final String regionName;
    private final LocalDate validUntil;
}
