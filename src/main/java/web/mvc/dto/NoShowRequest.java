package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 노쇼 처리 요청용
 * */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoShowRequest {
    private Long reservationId;
}
