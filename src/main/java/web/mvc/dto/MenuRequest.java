package web.mvc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 메뉴 생성·수정 요청 시 사용되는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequest {
    private String menuName;
    private Integer price;
    private String description;
}
