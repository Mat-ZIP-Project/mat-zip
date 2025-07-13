package web.mvc.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqReviewDTO {
    private Long restaurantId;
    private String visitDate;
    private String content;
    private int rating;
    private boolean local;



}
