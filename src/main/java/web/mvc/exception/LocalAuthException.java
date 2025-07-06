package web.mvc.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LocalAuthException extends RuntimeException implements ErrorCodeProvider{
	  private final ErrorCode errorCode;

	@Override
	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
