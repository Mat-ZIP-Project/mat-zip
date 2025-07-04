package web.mvc.service;

import web.mvc.domain.User;
import web.mvc.domain.Restaurant;
import web.mvc.domain.WaitingQueue;

import java.util.List;

public interface WaitingService {

    /**
     * 사용자 웨이팅 등록
     * - 해당 유저가 이미 '입장 대기' 상태의 웨이팅을 가진 경우 예외 발생
     * - restaurant의 가장 마지막 waitingNumber 다음 번호로 부여됨
     * - waiting_status 테이블도 함께 업데이트
     */
    WaitingQueue registerWaiting(User user, Restaurant restaurant, int numPeople);



    /**
     * 특정 웨이팅 상태 변경 (입장 완료, 노쇼)
     * - status가 '입장 대기'인 경우에만 가능
     * - 변경 시 waiting_status 카운트 감소
     */
    void updateWaitingStatus(Long waitingId, String newStatus);

    /**
     * 현재 로그인한 유저의 '입장 대기' 상태 웨이팅 단건 조회
     * - 마이페이지에서 사용
     * - 없으면 예외 발생
     */
    WaitingQueue getMyActiveWaiting(User user);

    /**
     * 특정 식당의 '입장 대기' 상태 웨이팅 리스트 조회
     * - 사장님 마이페이지에서 확인용
     * - 대기 순번 기준 정렬됨
     */
    List<WaitingQueue> getWaitingListByRestaurant(Restaurant restaurant);

    /**
     * 웨이팅 ID로 웨이팅 단건 조회
     * 컨트롤러에서 권한 체크나 상태 변경 전에 사용
     */
    WaitingQueue getWaitingById(Long waitingId);

}
