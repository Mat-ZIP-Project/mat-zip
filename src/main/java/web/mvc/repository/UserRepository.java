package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
