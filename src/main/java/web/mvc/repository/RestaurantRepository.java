package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.Restaurant;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // 카테고리와 지역(구) 모두 필터링
    List<Restaurant> findByCategoryAndRegionSigungu(String category, String regionSigungu);

    // 카테고리만 필터링
    List<Restaurant> findByCategory(String category);

    // 지역(구)만 필터링
    List<Restaurant> findByRegionSigungu(String regionSigungu);
}
