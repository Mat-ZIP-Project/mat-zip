package web.mvc.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import web.mvc.dto.ResponseErrorDTO;

@RestControllerAdvice
@Slf4j
public class GlobalRestExceptionAdvice {


	@ExceptionHandler(LocalAuthException.class)
	public ResponseEntity<?> error(LocalAuthException e) {

		log.error("Error Message error {} " , e.getErrorCode().getMsg() );

		return ResponseEntity
				.status(e.getErrorCode().getStatus())
				.body(ResponseErrorDTO.builder()
						.msg(e.getErrorCode().getMsg())
						.status(e.getErrorCode().getStatus())
						.build());
	}

}
