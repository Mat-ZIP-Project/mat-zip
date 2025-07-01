package web.mvc.service;

import web.mvc.dto.AddressResponse;
import web.mvc.dto.SignupOwnerRequest;
import web.mvc.dto.SignupRequest;

import java.util.List;

public interface SignupService {

    /** 아이디 중복체크 */
    void checkUserIdDuplicate(String userId);

    /** 휴대폰번호 중복체크 */
    void checkPhoneDuplicate(String phone);

    /** 비밀번호 유효성 체크 */
    void validatePassword(String password);

    /** 사업자등록번호 검증 (DB 중복체크 + API 검증) */
    void verifyBusinessNumber(String businessNumber);

    /** 회원가입 시 휴대폰 인증완료 여부 확인 */
    void checkSmsVerified(String phone, String purpose);

    /** 주소 검색 (카카오 맵 API) */
    List<AddressResponse> searchAddress(String query);

    /** 일반 사용자 회원가입 */
    void signupUser(SignupRequest request);

    /** 사업주 회원가입 */
    void signupOwner(SignupOwnerRequest request);
}
