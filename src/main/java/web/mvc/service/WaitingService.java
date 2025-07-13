package web.mvc.service;

import web.mvc.dto.WaitingRegisterRequestDTO;
import web.mvc.dto.WaitingRegisterResponseDTO;
import web.mvc.dto.WaitingStatusResponseDTO;

import java.util.List;

public interface WaitingService {

    /**
     * 웨이팅 등록
     * - 사용자가 특정 식당에 웨이팅을 신청할 때 호출
     */
    WaitingRegisterResponseDTO registerWaitingByUserId(String userId, WaitingRegisterRequestDTO dto);

    /**
     * 현재 로그인한 사용자의 웨이팅 상태 조회
     */
    WaitingStatusResponseDTO getMyWaitingStatus(String userId);

    /**
     * 다음 대기자 호출 처리
     * - 식당에서 다음 대기자를 호출하여 상태를 '호출됨(CALLED)'으로 변경
     */
    void callNextWaiting(Long restaurantId);

    /**
     * 호출된 대기자의 입장 처리
     * - 호출된 상태(CALLED)인 대기자가 실제로 입장했을 때 상태를 '입장 완료(ENTERED)'로 변경
     */
    void enterWaitingUser(Long waitingId);

    /**
     * 특정 식당의 전체 웨이팅 현황 조회
     * - 현재 대기 인원 수, 예상 입장 시간 등의 정보를 반환
     */
    WaitingStatusResponseDTO getWaitingStatusByRestaurantId(Long restaurantId);

    /**
     * 식당 주인이 등록한 식당의 웨이팅 현황 조회
     */
    List<WaitingStatusResponseDTO> getWaitingStatusesByOwnerId(String ownerId);

    /**
     * 특정 대기자를 노쇼(NO-SHOW) 처리
     * - 주인이 수동으로 호출한 대기자에 대해 노쇼 처리 시 사용
     */
    void markNoShow(Long waitingId);

    /**
     * 15분 이상 경과한 호출된 대기자들을 자동으로 노쇼 처리하는 스케줄러 메서드
     * - 일정 주기로 자동 실행되어 자동 노쇼 처리를 수행
     */
    void autoMarkNoShow();

}
