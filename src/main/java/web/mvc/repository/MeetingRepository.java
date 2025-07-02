package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.mvc.domain.Meeting;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
}
