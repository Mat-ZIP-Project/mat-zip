package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.mvc.domain.Restaurant;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // 카테고리와 지역(구) 모두 필터링
    List<Restaurant> findByCategoryInAndRegionSigungu(List<String> categories, String regionSigungu);

    // 카테고리만 필터링
    List<Restaurant> findByCategoryIn(List<String> categories);

    // 지역(구)만 필터링
    List<Restaurant> findByRegionSigungu(String regionSigungu);

    // 식당 Id로 식당 찾기
    Optional<Restaurant> findByRestaurantId(Long restaurantId);

    // 식당 이름으로 식당 조회 (여러 개 출력 가능)
    List<Restaurant> findByRestaurantNameContaining(String keyword);


    //좌표, 반경 기반 식당 리스트 조회
    @Query(value = "select * from restaurants " +
            "where ST_Distance_Sphere(\n" +
            "        POINT(longitude, latitude),\n" +
            "        POINT(:longitude, :latitude)\n" +
            "    ) < :radius;",nativeQuery = true)
    List<Restaurant> searchByPosition(@Param("longitude")  double longitude, @Param("latitude") double latitude, @Param("radius") long radius);

    /** 업주 ID(userId)에 매핑된 식당 조회 ( owner, user 조인한 식당 조회 ) */
    @Query("SELECT r FROM Restaurant r JOIN FETCH r.owner o " +
            "JOIN FETCH o.user u WHERE u.userId = :userId")
    Optional<Restaurant> findByOwnerUserId(@Param("userId") String userId);

    /** ownerId로 식당 조회  */
    @Query("SELECT r FROM Restaurant r WHERE r.owner.ownerId = :ownerId")
    Optional<Restaurant> findByOwnerId(@Param("ownerId") Long ownerId);

    // Restaurant 엔티티에서 owner.userId 기준으로 식당 리스트 조회
    List<Restaurant> findByOwner_User_UserId(String userId);

    // 1. [카테고리 기반 추천] - 찜 많은 순 상위 20
    @Query(value = """
    SELECT r.*
    FROM restaurants r
    LEFT JOIN user_likes ul ON r.restaurant_id = ul.restaurant_id
    WHERE r.category IN :categories
    GROUP BY r.restaurant_id
    ORDER BY COUNT(ul.like_id) DESC
    LIMIT 20
""", nativeQuery = true)
    List<Restaurant> findTop20ByCategoryInOrderByLikesDesc(@Param("categories") List<String> categories);


    // 2. [로컬 맛집 추천] - 로컬 평점 높은 순 상위 20
    @Query(value = """
    SELECT * FROM restaurants
    ORDER BY avg_rating_local DESC
    LIMIT 20
""", nativeQuery = true)
    List<Restaurant> findTop20ByAvgRatingLocalDesc();


    // 3. [인기 맛집 추천] - 예약 많은 순 상위 20
    @Query(value = """
    SELECT r.*
    FROM restaurants r
    LEFT JOIN reservations rs ON r.restaurant_id = rs.restaurant_id
    GROUP BY r.restaurant_id
    ORDER BY COUNT(rs.reservation_id) DESC
    LIMIT 20
""", nativeQuery = true)
    List<Restaurant> findTop20ByReservationCountDesc();


}
