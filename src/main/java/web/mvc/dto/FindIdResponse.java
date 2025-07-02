package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FindIdResponse {
    private String userId;
    private String maskedUserId;
}
