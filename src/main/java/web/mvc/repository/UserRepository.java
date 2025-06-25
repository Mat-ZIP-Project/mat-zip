package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.mvc.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

    /** 활성상태인 userId 찾기 */
    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.userStatus = '활성'")
    Optional<User> findActiveUserByUserId(@Param("userId") String userId);

    /** 아이디 중복체크 */

    /** 휴대폰번호 중복체크 */
    Optional<User> findByPhone(String phone);

}
