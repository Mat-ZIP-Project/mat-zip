package web.mvc.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthLogDTO {
    private String regionName;
    private Long id;
}
