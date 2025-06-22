package web.mvc.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseErrorDTO {
    private  int status;
    private  String msg;
}
