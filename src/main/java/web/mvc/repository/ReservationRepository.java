package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import web.mvc.domain.Reservation;
import web.mvc.domain.Restaurant;
import web.mvc.dto.DailyStatsDto;
import web.mvc.dto.MonthlyStatsDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long> {

    // 특정 사용자의 모든 예약 조회
    List<Reservation> findByUserIdAndStatus(Long id, String status);
    List<Reservation> findByUserIdAndStatusIn(Long id, List<String> status);
    List<Reservation> findByUserIdAndStatusIsNot(Long id, String status);

    // 특정 식당의 예약 단건 조회
    Optional<Reservation> findByReservationId(Long reservationId);

    /** 특정 식당의 대기 중인 예약 조회 */
    @Query("SELECT r FROM Reservation r WHERE r.restaurant.restaurantId = :restaurantId AND r.status = :status")
    List<Reservation> findReservationsByRestaurant(@Param("restaurantId") Long restaurantId,
                                                   @Param("status") String status);

    /** "예약완료"이면서 날짜가 지난 예약 조회 (노쇼 대상) */
    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.date < :date")
    List<Reservation> findReservationsByBeforeDate(@Param("status") String status,
                                                         @Param("date") String date );

    /** 특정 식당의 전체 예약 조회 (최신순 정렬) */
    @Query("SELECT DISTINCT r FROM Reservation r JOIN FETCH r.user WHERE r.restaurant.restaurantId = :restaurantId ORDER BY r.createdAt DESC")
    List<Reservation> findAllByRestaurantIdOrderByCreatedAtDesc(@Param("restaurantId") Long restaurantId);

    /** 식당의 당일 예약건 조회 (예약 시간기준 정렬) (yyyy-MM-dd) */
    @Query("SELECT DISTINCT r FROM Reservation r JOIN FETCH r.user WHERE r.restaurant.restaurantId = :restaurantId " +
            "  AND r.date = :targetDate ORDER BY r.time DESC")
    List<Reservation> findByRestaurantAndDate(@Param("restaurantId") Long restaurantId,
                                              @Param("targetDate") String targetDate);

    /** 특정 식당의 전체 예약 페이징 적용 */
    // @Query("SELECT r FROM Reservation r WHERE r.restaurant.restaurantId = :restaurantId ORDER BY r.createdAt DESC")
    // Page<Reservation> findAllByRestaurantId(@Param("restaurantId") Long restaurantId, Pageable pageable);

    // 기존 메서드 (예약 존재 여부만 확인 가능)
    boolean existsByUser_UserIdAndRestaurant_RestaurantIdAndStatus(String userUserId, Long restaurantRestaurantId, String status);
    boolean existsByUser_IdAndRestaurant_RestaurantIdAndStatus(Long userId, Long restaurantRestaurantId, String status);

    /**
     *  예약 알림을 보낼 대상을 조회하는 메서드
     *  상태가 'approved'이고, 아직 알림을 보내지 않은.
     *  현재 날짜에 해당하는 모든 예약 찾는다.
     *  스프링 스케줄러 필요한 것
     */
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.status = 'APPROVED' " +
            "AND r.reminded = false " + // 아직 알림을 보내지 않은 경우
            "AND r.date = :currentDate")
    List<Reservation> findReservationsForReminder(@Param("currentDate") String currentDate);

    int countByRestaurant(Restaurant restaurant);

    /** (식당 통계용) 일별 예약 건수·매출 (인터페이스 프로젝션) */
    @Query("""
      SELECT
        r.date                 AS date,
        COUNT(r)               AS reservationCount,
        COALESCE(SUM(p.finalPaymentAmount), 0) AS revenue
      FROM Reservation r
      LEFT JOIN ReservationPayment p ON p.reservation = r
      WHERE r.restaurant.restaurantId = :restaurantId
        AND r.date BETWEEN :from AND :to
      GROUP BY r.date
      ORDER BY r.date
    """)
    List<DailyStatsProjection> findDailyStatsByRestaurantId(
            @Param("restaurantId") Long restaurantId,
            @Param("from")         String from,
            @Param("to")           String to
    );

    /** (식당 통계용) 월별 예약 건수·매출 (인터페이스 프로젝션 + DATE→YEAR/MONTH) */
    @Query("""
      SELECT
        FUNCTION('YEAR',  FUNCTION('STR_TO_DATE', r.date, '%Y-%m-%d')) AS year,
        FUNCTION('MONTH', FUNCTION('STR_TO_DATE', r.date, '%Y-%m-%d')) AS month,
        COUNT(r)               AS reservationCount,
        COALESCE(SUM(p.finalPaymentAmount), 0) AS revenue
      FROM Reservation r
      LEFT JOIN ReservationPayment p ON p.reservation = r
      WHERE r.restaurant.restaurantId = :restaurantId
        AND FUNCTION('STR_TO_DATE', r.date, '%Y-%m-%d')
            BETWEEN FUNCTION('STR_TO_DATE', :from, '%Y-%m-%d')
                AND FUNCTION('STR_TO_DATE', :to, '%Y-%m-%d')
      GROUP BY
        FUNCTION('YEAR',  FUNCTION('STR_TO_DATE', r.date, '%Y-%m-%d')),
        FUNCTION('MONTH', FUNCTION('STR_TO_DATE', r.date, '%Y-%m-%d'))
      ORDER BY
        FUNCTION('YEAR',  FUNCTION('STR_TO_DATE', r.date, '%Y-%m-%d')),
        FUNCTION('MONTH', FUNCTION('STR_TO_DATE', r.date, '%Y-%m-%d'))
    """)
    List<MonthlyStatsProjection> findMonthlyStatsByRestaurantId(
            @Param("restaurantId") Long restaurantId,
            @Param("from")         String from,
            @Param("to")           String to
    );
}
