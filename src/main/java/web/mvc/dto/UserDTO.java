package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String userId;
    private String name;
    private String phone;
    private String role;
    private int pointBalance;
    private Boolean noShow;
    private Boolean gpsVerified;
    private String userGrade;
    private List<String> preferences;

}
