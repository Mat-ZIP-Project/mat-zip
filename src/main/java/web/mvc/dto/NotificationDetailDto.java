package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDetailDto {
    private Long notificationId;
    private String title;
    private String body;
    private boolean isRead;
    private LocalDateTime createdAt;

    // 식당 관련 정보
    private Long reservationId;
    private String restaurantName;
}
