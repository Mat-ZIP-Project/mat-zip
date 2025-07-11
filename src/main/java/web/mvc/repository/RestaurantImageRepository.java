package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.mvc.domain.RestaurantImage;
import web.mvc.domain.Restaurant;

import java.util.List;
import java.util.Optional;

public interface RestaurantImageRepository extends JpaRepository<RestaurantImage, Long> {
    /** 대표이미지 먼저, 그 다음 imageId 순으로 정렬 */
    @Query("SELECT ri FROM RestaurantImage ri WHERE ri.restaurant = :restaurant ORDER BY ri.isMain DESC, ri.imageId ASC")
    List<RestaurantImage> findAllByRestaurant(@Param("restaurant") Restaurant restaurant);

    Optional<RestaurantImage> findFirstByRestaurant(Restaurant restaurant);

    /** 대표이미지 조회 */
    Optional<RestaurantImage> findByRestaurantAndIsMainTrue(Restaurant restaurant);

    int countByRestaurant(Restaurant restaurant);
}
