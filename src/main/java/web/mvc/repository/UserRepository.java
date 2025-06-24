package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.mvc.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

    //사용자 활성 상태 체크
    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.userStatus = '활성'")
    Optional<User> findActiveUserByUserId(@Param("userId") String userId);
}
