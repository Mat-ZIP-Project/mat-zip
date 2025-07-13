package web.mvc.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ResLocalDTO {
    private boolean localReview;
    private String userId;
}
