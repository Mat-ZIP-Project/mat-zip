package web.mvc.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import web.mvc.domain.Restaurant;
import web.mvc.domain.User;
import web.mvc.domain.WaitingQueue;
import web.mvc.domain.WaitingStatus;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.WaitingQueueRepository;
import web.mvc.repository.WaitingStatusRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WaitingServiceImpl implements WaitingService {

    private final WaitingQueueRepository waitingQueueRepository;
    private final WaitingStatusRepository waitingStatusRepository;

    @Override
    @Transactional
    public WaitingQueue registerWaiting(User user, Restaurant restaurant, int numPeople) {
        // 입장 대기 중인 웨이팅 존재하는지 확인
        if (waitingQueueRepository.existsByUserAndStatus(user, "입장 대기")) {
            throw new BasicException(ErrorCode.WAITING_ALREADY_EXISTS);
        }

        // 현재 식당에서 가장 높은 waiting_number 조회
        Integer maxNumber = waitingQueueRepository.findMaxWaitingNumberByRestaurant(restaurant);
        int nextWaitingNumber = (maxNumber != null ? maxNumber : 0) + 1;

        // 웨이팅 등록
        WaitingQueue waiting = WaitingQueue.builder()
                .user(user)
                .restaurant(restaurant)
                .numPeople(numPeople)
                .waitingNumber(nextWaitingNumber)
                .waitTime(LocalDateTime.now())
                .status("입장 대기")
                .build();
        WaitingQueue saved = waitingQueueRepository.save(waiting);

        // waiting_status 테이블 갱신
        WaitingStatus status = waitingStatusRepository.findByRestaurant(restaurant)
                .orElse(WaitingStatus.builder()
                        .restaurant(restaurant)
                        .waitingCount(0)
                        .updatedAt(LocalDateTime.now())
                        .build());
        status.setWaitingCount(status.getWaitingCount() + 1);
        status.setUpdatedAt(LocalDateTime.now());
        waitingStatusRepository.save(status);

        return saved;
    }

    @Override
    @Transactional
    public void updateWaitingStatus(Long waitingId, String newStatus) {
        WaitingQueue waiting = waitingQueueRepository.findById(waitingId)
                .orElseThrow(() -> new BasicException(ErrorCode.WAITING_NOT_FOUND));

        if ("입장 대기".equals(waiting.getStatus())) {
            // 상태를 '입장 완료' 또는 '노쇼'로 변경
            waiting.setStatus(newStatus);
            waitingQueueRepository.save(waiting);

            // waiting_status count 감소
            WaitingStatus status = waitingStatusRepository.findByRestaurant(waiting.getRestaurant())
                    .orElseThrow(() -> new IllegalStateException("waiting_status가 존재하지 않습니다."));
            status.setWaitingCount(Math.max(0, status.getWaitingCount() - 1));
            status.setUpdatedAt(LocalDateTime.now());
            waitingStatusRepository.save(status);
        }
    }

    @Override
    public WaitingQueue getMyActiveWaiting(User user) {
        return waitingQueueRepository.findByUserAndStatus(user, "입장 대기")
                .orElseThrow(() -> new BasicException(ErrorCode.WAITING_NOT_FOUND));
    }

    @Override
    public List<WaitingQueue> getWaitingListByRestaurant(Restaurant restaurant) {
        return waitingQueueRepository.findByRestaurantAndStatusOrderByWaitingNumber(restaurant, "입장 대기");
    }

    @Override
    public WaitingQueue getWaitingById(Long waitingId) {
        return waitingQueueRepository.findById(waitingId)
                .orElseThrow(() -> new BasicException(ErrorCode.WAITING_NOT_FOUND));
    }


}
