package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.UserLocalBadge;

public interface LocalBadgeRepository extends JpaRepository<UserLocalBadge,Long> {

}
