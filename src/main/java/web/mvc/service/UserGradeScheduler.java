package web.mvc.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.User;
import web.mvc.exception.BasicException;
import web.mvc.repository.PointRepository;
import web.mvc.repository.UserRepository;

import java.util.List;
import java.util.logging.Logger;

@Component
@Slf4j
public class UserGradeScheduler {

    private final UserRepository userRepository;
    private final PointRepository pointRepository;

    public UserGradeScheduler(UserRepository userRepository, PointRepository pointRepository) {
        this.userRepository = userRepository;
        this.pointRepository = pointRepository;
    }

    /**
     * 매일 오전 3시에 이 메서드는 사용자 등급을 확인하고 업데이트합니다.
     * cron 표현식 "0 0 3 * * ?"의 의미:
     * - 초: 0
     * - 분: 0
     * - 시: 3 (오전 3시)
     * - 월의 일: * (매일)
     * - 월: * (매월)
     * - 요일: ? (요일은 지정하지 않음 - 월의 일과 함께 사용)
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 오전 3시에 실행
    @Transactional
    public void updateUserGradesScheduled() {
        log.info("예약된 사용자 등급 업데이트 프로세스 시작...");
        List<User> users = userRepository.findAll(); // 또는 업데이트가 필요할 수 있는 사용자만 찾기

        for (User user : users) {
            try {
                checkAndUpdateUserGrade(user);
            } catch (BasicException e) {
                log.error("사용자 {}의 등급 업데이트 오류: {}", user.getUserId(), e.getMessage());
            } catch (Exception e) {
                log.error("사용자 {}의 등급 업데이트 중 예상치 못한 오류 발생: {}", user.getUserId(), e.getMessage(), e);
            }
        }
        log.info("예약된 사용자 등급 업데이트 프로세스 완료.");
    }

    // 스케줄러에 맞게 약간 수정된 기존 로직
    private void checkAndUpdateUserGrade(User user) throws BasicException {
        String currentGrade = user.getUserGrade();

        int maxPoints = pointRepository.findMaxPointLogByUser(user);

        String newGrade = currentGrade;

        // 등급 상승 로직
        if ("브론즈".equals(currentGrade) && maxPoints >= 3000) {
            newGrade = "실버";
        } else if ("실버".equals(currentGrade) && maxPoints >= 10000) {
            newGrade = "먹짱";
        }

        if (!currentGrade.equals(newGrade)) {
            user.setUserGrade(newGrade);
            userRepository.save(user); // 이것은 데이터베이스에서 사용자를 업데이트합니다.
            log.info("사용자 {}의 등급이 {}에서 {}로 상승했습니다.", user.getUserId(), currentGrade, newGrade);
        } else {
            log.info("사용자 {}의 등급은 {}로 유지됩니다. 변경 사항 없음.", user.getUserId(), currentGrade);
        }
    }
}
