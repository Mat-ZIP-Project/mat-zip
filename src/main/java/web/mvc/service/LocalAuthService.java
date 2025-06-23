package web.mvc.service;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import web.mvc.domain.UserLocalAuth;
import web.mvc.dto.AuthLogDTO;
import web.mvc.dto.ResAuthDTO;
import web.mvc.dto.ResBadgeDTO;

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

    /**
     * 로컬 인증 뱃지 검색
     */
    List<ResBadgeDTO> searchBadges(Long id);

}
