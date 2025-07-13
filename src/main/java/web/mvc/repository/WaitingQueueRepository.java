package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import web.mvc.domain.WaitingQueue;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface WaitingQueueRepository extends JpaRepository<WaitingQueue, Long> {

    // 현재 사용자가 웨이팅 중인지 여부 (다른 식당 포함)
    boolean existsByUser_UserIdAndStatus(String userId, String status);

    // 특정 식당의 현재 대기 목록 (상태: 입장 대기)
    List<WaitingQueue> findByRestaurant_RestaurantIdAndStatusOrderByWaitingNumberAsc(Long restaurantId, String status);

    // 특정 식당에서 가장 큰 waiting_number 조회 → 신규 대기자 번호 계산용
    Optional<WaitingQueue> findTopByRestaurant_RestaurantIdOrderByWaitingNumberDesc(Long restaurantId);

    // 사용자의 현재 웨이팅 조회 (주로 상태: 입장 대기)
    Optional<WaitingQueue> findByUser_UserIdAndStatus(String userId, String status);

    // 대기 상태 중 expectedEntryTime이 지나도 입장 안 한 경우 찾기 (노쇼 후보)
    @Query("SELECT w FROM WaitingQueue w WHERE w.status = '입장 대기' AND w.expectedEntryTime < CURRENT_TIMESTAMP")
    List<WaitingQueue> findExpiredWaitings();

    List<WaitingQueue> findByStatusAndCalledAtBefore(String status, LocalDateTime beforeTime);
}
