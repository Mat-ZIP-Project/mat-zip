package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import web.mvc.domain.UserLike;
import web.mvc.domain.User;
import web.mvc.domain.Restaurant;

import java.util.Optional;
import java.util.List;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {

    Optional<UserLike> findByUserAndRestaurant(User user, Restaurant restaurant);

    List<UserLike> findAllByUser(User user);

    int countByRestaurant(Restaurant restaurant);

    @Query("SELECT l.restaurant.restaurantId FROM UserLike l WHERE l.user.id = :userId")
    List<Long> findLikedRestaurantIdsByUserId(@Param("userId") Long userId);
}
