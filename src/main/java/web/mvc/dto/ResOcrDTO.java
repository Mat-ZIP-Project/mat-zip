package web.mvc.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResOcrDTO {
    private String restaurantName;
    private String visitDate;
}

