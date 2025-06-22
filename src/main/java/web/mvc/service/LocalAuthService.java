package web.mvc.service;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import web.mvc.domain.UserLocalAuth;
import web.mvc.dto.AuthLogDTO;
import web.mvc.dto.ResAuthDTO;

import java.util.List;

public interface LocalAuthService {
    /**
     * 인증하기
     */
    String insertAuthLog(AuthLogDTO authLogDTO);
    /**
     * 인증기록 검색
     */
    List<ResAuthDTO> searchAuthLogs(Long id);

}
