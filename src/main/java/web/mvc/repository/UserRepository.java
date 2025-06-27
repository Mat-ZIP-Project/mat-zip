package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.mvc.domain.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** userId로 사용자 조회 */
    Optional<User> findByUserId(String userId);

    /** 활성상태인 userId 찾기 */
    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.userStatus = '활성'")
    Optional<User> findActiveUserByUserId(@Param("userId") String userId);

    /** 휴대폰번호로 사용자 조회 */
    Optional<User> findByPhone(String phone);

    /** 아이디 중복체크 */
    boolean existsByUserId(String userId);

    /** 휴대폰번호 중복체크 */
    boolean existsByPhone(String phone);

}
