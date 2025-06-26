package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.UserLike;
import web.mvc.domain.User;
import web.mvc.domain.Restaurant;

import java.util.Optional;
import java.util.List;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {

    Optional<UserLike> findByUserAndRestaurant(User user, Restaurant restaurant);

    List<UserLike> findAllByUser(User user);

    int countByRestaurant(Restaurant restaurant);
}
