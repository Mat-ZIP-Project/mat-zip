package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import web.mvc.domain.Restaurant;
import web.mvc.domain.User;
import web.mvc.domain.WaitingQueue;

import java.util.List;
import java.util.Optional;

public interface WaitingQueueRepository extends JpaRepository<WaitingQueue, Long> {

    // 특정 식당의 가장 마지막 웨이팅 번호 조회 (최댓값)
    @Query("SELECT MAX(w.waitingNumber) FROM WaitingQueue w WHERE w.restaurant = :restaurant")
    Integer findMaxWaitingNumberByRestaurant(Restaurant restaurant);

    // 유저가 현재 '입장 대기' 상태인 웨이팅이 있는지 확인
    boolean existsByUserAndStatus(User user, String status);

    // 특정 식당의 대기 인원 수 조회 (status = '입장 대기'인 경우만)
//    int countByRestaurantAndStatus(Restaurant restaurant, String status);

    // 특정 식당의 '입장 대기' 웨이팅 전체 조회
    List<WaitingQueue> findByRestaurantAndStatusOrderByWaitingNumber(Restaurant restaurant, String status);

    // 유저가 등록한 웨이팅 리스트
//    List<WaitingQueue> findByUser(User user);

    // 특정 유저 + 식당의 현재 '입장 대기' 웨이팅 조회
    Optional<WaitingQueue> findByUserAndStatus(User user, String status);



}
