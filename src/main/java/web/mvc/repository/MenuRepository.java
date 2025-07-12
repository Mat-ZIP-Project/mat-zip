package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.mvc.domain.Menu;
import web.mvc.domain.Restaurant;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    /** 특정 식당에 속한 메뉴 목록 조회 */
    List<Menu> findAllByRestaurant(Restaurant restaurant);

    /** 업주 ID(userId)에 매핑된 식당의 메뉴 조회 */
    @Query("SELECT m FROM Menu m JOIN FETCH m.restaurant r " +
            "JOIN FETCH r.owner o JOIN FETCH o.user u " +
            "WHERE u.userId = :userId")
    List<Menu> findByOwnerUserId(@Param("userId") String userId);

}
