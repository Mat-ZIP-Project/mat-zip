package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.OwnerInfo;

public interface OwnerInfoRepository extends JpaRepository<OwnerInfo, Long> {
    /** 사업자 등록번호 중복체크 */
    boolean existsByBusinessNumber(String businessNumber);
}
