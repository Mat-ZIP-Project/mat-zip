package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.RestaurantImage;
import web.mvc.domain.Restaurant;

import java.util.List;
import java.util.Optional;

public interface RestaurantImageRepository extends JpaRepository<RestaurantImage, Long> {
    List<RestaurantImage> findAllByRestaurant(Restaurant restaurant);

    Optional<RestaurantImage> findFirstByRestaurant(Restaurant restaurant);
}
