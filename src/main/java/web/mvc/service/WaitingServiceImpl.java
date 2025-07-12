package web.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.Restaurant;
import web.mvc.domain.User;
import web.mvc.domain.WaitingQueue;
import web.mvc.domain.WaitingStatus;
import web.mvc.dto.WaitingRegisterRequestDTO;
import web.mvc.dto.WaitingRegisterResponseDTO;
import web.mvc.dto.WaitingStatusResponseDTO;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.RestaurantRepository;
import web.mvc.repository.UserRepository;
import web.mvc.repository.WaitingQueueRepository;
import web.mvc.repository.WaitingStatusRepository;
import web.mvc.util.WaitingConstants;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitingServiceImpl implements WaitingService {

    private final WaitingQueueRepository waitingQueueRepository;
    private final WaitingStatusRepository waitingStatusRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    public WaitingRegisterResponseDTO registerWaitingByUserId(String userId, WaitingRegisterRequestDTO dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BasicException(ErrorCode.USER_NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        // 이미 다른 식당에 웨이팅 중인지 확인 (입장 대기 상태)
        boolean alreadyWaiting = waitingQueueRepository.existsByUser_UserIdAndStatus(userId, WaitingConstants.STATUS_WAITING);
        if (alreadyWaiting) {
            throw new BasicException(ErrorCode.WAITING_ALREADY_EXISTS);
        }

        // 식당 최대 웨이팅 제한 확인
        int currentWaitingCount = waitingQueueRepository
                .findByRestaurant_RestaurantIdAndStatusOrderByWaitingNumberAsc(restaurant.getRestaurantId(), WaitingConstants.STATUS_WAITING)
                .size();

        if (restaurant.getMaxWaitingLimit() != null && currentWaitingCount >= restaurant.getMaxWaitingLimit()) {
            throw new BasicException(ErrorCode.MAX_WAITING_LIMIT_EXCEEDED);
        }

        // waitingNumber 계산
        int nextWaitingNumber = waitingQueueRepository
                .findTopByRestaurant_RestaurantIdOrderByWaitingNumberDesc(restaurant.getRestaurantId())
                .map(w -> w.getWaitingNumber() + 1)
                .orElse(1);

        // 예상 입장 시간 계산 (예: 5분 * 앞 대기자 수)
        LocalDateTime expectedTime = LocalDateTime.now().plusMinutes(5L * currentWaitingCount);

        WaitingQueue waiting = WaitingQueue.builder()
                .user(user)
                .restaurant(restaurant)
                .numPeople(dto.getNumPeople())
                .waitingNumber(nextWaitingNumber)
                .expectedEntryTime(expectedTime)
                .status(WaitingConstants.STATUS_WAITING)
                .build();

        waitingQueueRepository.save(waiting);

        // waiting_status 저장 또는 업데이트
        WaitingStatus status = waitingStatusRepository.findByRestaurant_RestaurantId(restaurant.getRestaurantId())
                .orElse(WaitingStatus.builder()
                        .restaurant(restaurant)
                        .restaurantId(restaurant.getRestaurantId())
                        .waitingCount(0)
                        .build());

        status.setWaitingCount(status.getWaitingCount() + 1);
        status.setUpdatedAt(LocalDateTime.now());
        waitingStatusRepository.save(status);

        return WaitingRegisterResponseDTO.builder()
                .waitingNumber(nextWaitingNumber)
                .waitingOrder(currentWaitingCount + 1)
                .expectedEntryTime(expectedTime)
                .status(WaitingConstants.STATUS_WAITING)
                .build();
    }

    @Override
    public WaitingStatusResponseDTO getMyWaitingStatus(String userId) {
        WaitingQueue myWaiting = waitingQueueRepository
                .findByUser_UserIdAndStatus(userId, WaitingConstants.STATUS_WAITING)
                .orElseThrow(() -> new BasicException(ErrorCode.WAITING_NOT_FOUND));

        Long restaurantId = myWaiting.getRestaurant().getRestaurantId();
        String restaurantName = myWaiting.getRestaurant().getRestaurantName();
        int myNumber = myWaiting.getWaitingNumber();

        List<WaitingQueue> allWaitings = waitingQueueRepository
                .findByRestaurant_RestaurantIdAndStatusOrderByWaitingNumberAsc(restaurantId, WaitingConstants.STATUS_WAITING);

        int position = 1;
        for (WaitingQueue w : allWaitings) {
            if (w.getWaitingNumber().equals(myNumber)) break;
            position++;
        }

        return WaitingStatusResponseDTO.builder()
                .restaurantName(restaurantName)
                .waitingNumber(myNumber)
                .waitingOrder(position)
                .status(myWaiting.getStatus())
                .expectedEntryTime(myWaiting.getExpectedEntryTime())
                .waitingCount(allWaitings.size())
                .build();
    }

    @Override
    public void callNextWaiting(Long restaurantId) {
        List<WaitingQueue> waitingList = waitingQueueRepository
                .findByRestaurant_RestaurantIdAndStatusOrderByWaitingNumberAsc(restaurantId, WaitingConstants.STATUS_WAITING);

        if (waitingList.isEmpty()) {
            throw new BasicException(ErrorCode.NO_WAITING_AVAILABLE);
        }

        WaitingQueue first = waitingList.get(0);
        first.setStatus(WaitingConstants.STATUS_CALLED);
        first.setCalledAt(LocalDateTime.now());
        waitingQueueRepository.save(first);

        WaitingStatus status = waitingStatusRepository.findByRestaurant_RestaurantId(restaurantId)
                .orElseThrow(() -> new BasicException(ErrorCode.WAITING_STATUS_NOT_FOUND));
        status.setWaitingCount(Math.max(0, status.getWaitingCount() - 1));
        status.setUpdatedAt(LocalDateTime.now());
        waitingStatusRepository.save(status);
    }

    @Override
    public void enterWaitingUser(Long waitingId) {
        WaitingQueue waiting = waitingQueueRepository.findById(waitingId)
                .orElseThrow(() -> new BasicException(ErrorCode.WAITING_NOT_FOUND));

        if (!waiting.getStatus().equals(WaitingConstants.STATUS_CALLED)) {
            throw new BasicException(ErrorCode.INVALID_STATUS_CHANGE);
        }

        waiting.setStatus(WaitingConstants.STATUS_ENTERED);
        waitingQueueRepository.save(waiting);
    }

    @Override
    public WaitingStatusResponseDTO getWaitingStatusByRestaurantId(Long restaurantId) {
        WaitingStatus status = waitingStatusRepository.findByRestaurant_RestaurantId(restaurantId)
                .orElseThrow(() -> new BasicException(ErrorCode.WAITING_STATUS_NOT_FOUND));

        int waitingCount = status.getWaitingCount();
        LocalDateTime updatedAt = status.getUpdatedAt();
        long estimatedTime = waitingCount * 5L; // 5분 per 팀

        return WaitingStatusResponseDTO.builder()
                .restaurantName(status.getRestaurant().getRestaurantName())
                .waitingCount(waitingCount)
                .expectedEntryTime(LocalDateTime.now().plusMinutes(estimatedTime))
                .status("현황")
                .build();
    }

}
