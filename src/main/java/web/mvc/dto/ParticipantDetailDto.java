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
public class ParticipantDetailDto {
    private Long joinId;
    private String joinStatus;
    private LocalDateTime joinedAt;

    // Meeting 엔티티에서 가져올 정보
    private Long meetingId;
    private String title;
    private String description;
    private Integer currentParticipants;
    private LocalDateTime meetingTime;
    private String restaurantName;
}
