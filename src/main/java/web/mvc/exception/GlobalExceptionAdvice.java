package web.mvc.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {
	
	@ExceptionHandler(BasicException.class)
	public ModelAndView error(BasicException e) {
		
      log.error("Error Message error {} " , e.getErrorCode().getMsg() );
		
		//예외가 발생했을때 해야할 일작성
		ModelAndView mv = new ModelAndView("error/errorView");
		mv.addObject("errMessage", e.getErrorCode().getMsg());
		mv.addObject("errStatus", e.getErrorCode().getStatus());
		
		return mv;
	}

}
