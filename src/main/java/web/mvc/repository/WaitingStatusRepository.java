package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.Restaurant;
import web.mvc.domain.WaitingStatus;

import java.util.Optional;

public interface WaitingStatusRepository extends JpaRepository<WaitingStatus, Long> {

    // 식당 ID로 조회
    Optional<WaitingStatus> findByRestaurant(Restaurant restaurant);
}
