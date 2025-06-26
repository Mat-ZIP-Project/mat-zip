package web.mvc.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifySmsRequest {
    private String phone;
    private String code;
    private String purpose;
}
