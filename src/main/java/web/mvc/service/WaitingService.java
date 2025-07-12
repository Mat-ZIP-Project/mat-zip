package web.mvc.service;


import web.mvc.dto.WaitingRegisterRequestDTO;
import web.mvc.dto.WaitingRegisterResponseDTO;
import web.mvc.dto.WaitingStatusResponseDTO;

public interface WaitingService {

    /**
     * 웨이팅 등록
     */
    WaitingRegisterResponseDTO registerWaitingByUserId(String userId, WaitingRegisterRequestDTO dto);

    /**
     * 현재 내 대기 상태 조회
     */
    WaitingStatusResponseDTO getMyWaitingStatus(String userId);

    /**
     * 다음 대기자 입장 처리
     */
    void callNextWaiting(Long restaurantId);

    void enterWaitingUser(Long waitingId);

    WaitingStatusResponseDTO getWaitingStatusByRestaurantId(Long restaurantId);

}