package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}
