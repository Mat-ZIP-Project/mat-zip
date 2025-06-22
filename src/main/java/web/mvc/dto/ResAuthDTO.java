package web.mvc.dto;

import lombok.*;
import org.springframework.stereotype.Component;

@Component
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ResAuthDTO {
    private String regionName;
    private Long authCount;
}
