package web.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private boolean success;
    private String message;
    private T data;

    // T 타입의 데이터를 받아서 Response<T>를 반환하도록 수정
    public static <T> Response<T> success(T data) {

        return new Response<>(true, "성공", data);
    }
    // 데이터 없이 성공 메시지만 반환할 때
    public static Response<Void> success(String message) {
        return new Response<>(true, message, null);
    }
    public static Response<String> fail(String message) {
        return new Response<>(false, message, null);
    }
}
