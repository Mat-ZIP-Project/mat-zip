package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import web.mvc.domain.UserLocalAuth;
import web.mvc.dto.ResAuthDTO;

public interface LocalAuthRepository extends JpaRepository<UserLocalAuth, Long> , QuerydslPredicateExecutor<UserLocalAuth> {

}
