package web.mvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import web.mvc.util.SseEmitterManager;
import web.mvc.util.WaitingConstants;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 웨이팅 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WaitingServiceImpl implements WaitingService {

    private final WaitingQueueRepository waitingQueueRepository;
    private final WaitingStatusRepository waitingStatusRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final SseEmitterManager sseEmitterManager;


    /**
     * 유저가 웨이팅 등록
     */
    @Override
    public WaitingRegisterResponseDTO registerWaitingByUserId(String userId, WaitingRegisterRequestDTO dto) {
        // 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BasicException(ErrorCode.USER_NOT_FOUND));

        // 식당 조회
        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

        // 유저가 다른 식당에서 이미 웨이팅 중인지 확인
        boolean alreadyWaiting = waitingQueueRepository.existsByUser_UserIdAndStatus(userId, WaitingConstants.STATUS_WAITING);
        if (alreadyWaiting) {
            throw new BasicException(ErrorCode.WAITING_ALREADY_EXISTS);
        }

        // 현재 식당의 웨이팅 인원 수 확인
        int currentWaitingCount = waitingQueueRepository
                .findByRestaurant_RestaurantIdAndStatusOrderByWaitingNumberAsc(restaurant.getRestaurantId(), WaitingConstants.STATUS_WAITING)
                .size();

        // 식당 최대 웨이팅 수 초과 시 예외
        if (restaurant.getMaxWaitingLimit() != null && currentWaitingCount >= restaurant.getMaxWaitingLimit()) {
            throw new BasicException(ErrorCode.MAX_WAITING_LIMIT_EXCEEDED);
        }

        // 다음 웨이팅 번호 계산 (가장 큰 번호 + 1)
        int nextWaitingNumber = waitingQueueRepository
                .findTopByRestaurant_RestaurantIdOrderByWaitingNumberDesc(restaurant.getRestaurantId())
                .map(w -> w.getWaitingNumber() + 1)
                .orElse(1); // 없으면 1번부터 시작

        // 예상 입장 시간 계산: 5분 * 현재 대기자 수
        LocalDateTime expectedTime = LocalDateTime.now().plusMinutes(5L * currentWaitingCount);

        // 대기 정보 생성 및 저장
        WaitingQueue waiting = WaitingQueue.builder()
                .user(user)
                .restaurant(restaurant)
                .numPeople(dto.getNumPeople())
                .waitingNumber(nextWaitingNumber)
                .expectedEntryTime(expectedTime)
                .status(WaitingConstants.STATUS_WAITING)
                .build();

        waitingQueueRepository.save(waiting);

        // waiting_status 테이블 업데이트 or 신규 생성
        WaitingStatus status = waitingStatusRepository.findByRestaurant_RestaurantId(restaurant.getRestaurantId())
                .orElse(WaitingStatus.builder()
                        .restaurant(restaurant)
                        .restaurantId(restaurant.getRestaurantId())
                        .waitingCount(0)
                        .build());

        // 대기 인원 수 증가 및 시간 갱신
        status.setWaitingCount(status.getWaitingCount() + 1);
        status.setUpdatedAt(LocalDateTime.now());
        waitingStatusRepository.save(status);

        // 응답 DTO 반환
        return WaitingRegisterResponseDTO.builder()
                .waitingNumber(nextWaitingNumber)
                .waitingOrder(currentWaitingCount + 1)
                .expectedEntryTime(expectedTime)
                .status(WaitingConstants.STATUS_WAITING)
                .build();
    }

    /**
     * 나의 웨이팅 상태 조회
     */
    @Override
    public WaitingStatusResponseDTO getMyWaitingStatus(String userId) {
        // 나의 웨이팅 조회 (입장 대기 상태)
        WaitingQueue myWaiting = waitingQueueRepository
                .findByUser_UserIdAndStatus(userId, WaitingConstants.STATUS_WAITING)
                .orElseThrow(() -> new BasicException(ErrorCode.WAITING_NOT_FOUND));

        Long restaurantId = myWaiting.getRestaurant().getRestaurantId();
        String restaurantName = myWaiting.getRestaurant().getRestaurantName();
        int myNumber = myWaiting.getWaitingNumber();

        // 해당 식당 전체 웨이팅 목록 가져오기 (번호순)
        List<WaitingQueue> allWaitings = waitingQueueRepository
                .findByRestaurant_RestaurantIdAndStatusOrderByWaitingNumberAsc(restaurantId, WaitingConstants.STATUS_WAITING);

        // 나의 순번 계산
        int position = 1;
        for (WaitingQueue w : allWaitings) {
            if (w.getWaitingNumber().equals(myNumber)) break;
            position++;
        }

        // 응답 DTO 생성
        return WaitingStatusResponseDTO.builder()
                .restaurantName(restaurantName)
                .waitingNumber(myNumber)
                .waitingOrder(position)
                .status(myWaiting.getStatus())
                .expectedEntryTime(myWaiting.getExpectedEntryTime())
                .waitingCount(allWaitings.size())
                .build();
    }

    /**
     * 다음 웨이팅 유저 호출
     */
    @Override
    public void callNextWaiting(Long restaurantId) {
        // 해당 식당의 웨이팅 리스트 조회
        List<WaitingQueue> waitingList = waitingQueueRepository
                .findByRestaurant_RestaurantIdAndStatusOrderByWaitingNumberAsc(restaurantId, WaitingConstants.STATUS_WAITING);

        // 대기자가 없으면 예외
        if (waitingList.isEmpty()) {
            throw new BasicException(ErrorCode.NO_WAITING_AVAILABLE);
        }

        // 가장 앞선 대기자 상태 '호출됨'으로 변경
        WaitingQueue first = waitingList.get(0);
        first.setStatus(WaitingConstants.STATUS_CALLED);
        first.setCalledAt(LocalDateTime.now());
        waitingQueueRepository.save(first);

        // ✅ 호출된 사용자에게 실시간 알림 전송 (SSE)
        sseEmitterManager.sendToUser(first.getUser().getUserId(), "당신의 차례입니다!");

        // waiting_status 대기자 수 감소 및 시간 갱신
        WaitingStatus status = waitingStatusRepository.findByRestaurant_RestaurantId(restaurantId)
                .orElseThrow(() -> new BasicException(ErrorCode.WAITING_STATUS_NOT_FOUND));
        status.setWaitingCount(Math.max(0, status.getWaitingCount() - 1));
        status.setUpdatedAt(LocalDateTime.now());
        waitingStatusRepository.save(status);
    }

    /**
     * 호출된 웨이팅 유저가 실제 입장 시 처리
     */
    @Override
    public void enterWaitingUser(Long waitingId) {
        // 해당 웨이팅 엔티티 조회
        WaitingQueue waiting = waitingQueueRepository.findById(waitingId)
                .orElseThrow(() -> new BasicException(ErrorCode.WAITING_NOT_FOUND));

        // '호출됨' 상태인 경우만 입장 가능
        if (!waiting.getStatus().equals(WaitingConstants.STATUS_CALLED)) {
            throw new BasicException(ErrorCode.INVALID_STATUS_CHANGE);
        }

        // 상태를 '입장 완료'로 변경
        waiting.setStatus(WaitingConstants.STATUS_ENTERED);
        waitingQueueRepository.save(waiting);
    }

    /**
     * 식당별 전체 웨이팅 현황 조회
     */
    @Override
    public WaitingStatusResponseDTO getWaitingStatusByRestaurantId(Long restaurantId) {
        // 해당 식당의 웨이팅 상태 조회
        WaitingStatus status = waitingStatusRepository.findByRestaurant_RestaurantId(restaurantId)
                .orElseThrow(() -> new BasicException(ErrorCode.WAITING_STATUS_NOT_FOUND));

        int waitingCount = status.getWaitingCount();
        LocalDateTime updatedAt = status.getUpdatedAt();

        // 예상 입장 시간 계산
        long estimatedTime = waitingCount * 5L; // 1팀당 5분 가정

        // 응답 DTO 반환
        return WaitingStatusResponseDTO.builder()
                .restaurantName(status.getRestaurant().getRestaurantName())
                .waitingCount(waitingCount)
                .expectedEntryTime(LocalDateTime.now().plusMinutes(estimatedTime))
                .status("현황") // 단순 문자열 상태
                .build();
    }

    /**
     * 식당 주인이 등록한 모든 식당의 웨이팅 현황 조회
     */
    @Override
    public List<WaitingStatusResponseDTO> getWaitingStatusesByOwnerId(String ownerId) {
        // 사용자가 소유한 식당 목록 조회
        List<Restaurant> myRestaurants = restaurantRepository.findByOwner_User_UserId(ownerId);

        return myRestaurants.stream().map(restaurant -> {
            // 각 식당의 웨이팅 현황 가져오기
            WaitingStatus status = waitingStatusRepository.findByRestaurant_RestaurantId(restaurant.getRestaurantId())
                    .orElse(WaitingStatus.builder()
                            .restaurant(restaurant)
                            .restaurantId(restaurant.getRestaurantId())
                            .waitingCount(0)
                            .updatedAt(LocalDateTime.now())
                            .build());

            return WaitingStatusResponseDTO.builder()
                    .restaurantName(restaurant.getRestaurantName())
                    .waitingCount(status.getWaitingCount())
                    .expectedEntryTime(LocalDateTime.now().plusMinutes(status.getWaitingCount() * 5L))
                    .status("현황")
                    .build();
        }).toList();
    }

    /**
     * 식당 주인이 직접 노쇼 처리 시도
     * 15분 이내라면 예외 발생
     */
    public void markNoShow(Long waitingId) {
        WaitingQueue waiting = waitingQueueRepository.findById(waitingId)
                .orElseThrow(() -> new BasicException(ErrorCode.WAITING_NOT_FOUND));

        if (!waiting.getStatus().equals(WaitingConstants.STATUS_CALLED)) {
            throw new BasicException(ErrorCode.INVALID_STATUS_CHANGE);
        }

        LocalDateTime calledAt = waiting.getCalledAt();
        if (calledAt == null || calledAt.plusMinutes(15).isAfter(LocalDateTime.now())) {
            throw new BasicException(ErrorCode.NOT_EXPIRED_YET); // 15분 안 지남
        }

        waiting.setStatus(WaitingConstants.STATUS_NOSHOW);
        waitingQueueRepository.save(waiting);
    }

    /**
     * 15분 이상 경과한 호출된 대기자 자동 노쇼 처리 (스케줄러)
     * 매 5분마다 실행 (cron 표현식 변경 가능)
     */
    @Scheduled(fixedDelay = 300000) // 300,000ms = 5분 간격 실행
    public void autoMarkNoShow() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);

        // 15분 이전에 호출된 상태인 대기자 리스트 조회
        List<WaitingQueue> expiredCalledList = waitingQueueRepository
                .findByStatusAndCalledAtBefore(WaitingConstants.STATUS_CALLED, threshold);

        if (expiredCalledList.isEmpty()) {
            log.info("autoMarkNoShow: 15분 지난 호출된 대기자가 없습니다. [{}]", LocalDateTime.now());
            return;
        }

        for (WaitingQueue waiting : expiredCalledList) {
            waiting.setStatus(WaitingConstants.STATUS_NOSHOW);
            waitingQueueRepository.save(waiting);
            log.info("autoMarkNoShow: waitingId={} 노쇼 처리됨 [{}]", waiting.getWaitingId(), LocalDateTime.now());
        }
    }
}
