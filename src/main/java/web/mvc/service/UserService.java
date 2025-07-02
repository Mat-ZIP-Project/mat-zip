package web.mvc.service;

import web.mvc.domain.User;
import web.mvc.dto.*;
import web.mvc.exception.BasicException;

public interface UserService {

    /** 아이디 찾기 */
    FindIdResponse findUserId(String phone);

    /** 비밀번호 찾기 - 아이디/휴대폰 매칭 확인 */
    void validateUserForPasswordReset(FindPasswordRequest request);

    /** 비밀번호 재설정 */
    void resetPassword(FindPasswordRequest request);

    /** 회원탈퇴 (Soft delete) */
    void withdrawUser(User user, WithdrawUserRequest request);
}
