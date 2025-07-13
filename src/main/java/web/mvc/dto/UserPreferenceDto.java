package web.mvc.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceDto {
    private String preferenceCategory;
}
