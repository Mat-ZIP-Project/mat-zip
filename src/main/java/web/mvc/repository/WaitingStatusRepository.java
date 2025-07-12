package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.WaitingStatus;

import java.util.Optional;

public interface WaitingStatusRepository extends JpaRepository<WaitingStatus, Long> {

    Optional<WaitingStatus> findByRestaurant_RestaurantId(Long restaurantId);
}
