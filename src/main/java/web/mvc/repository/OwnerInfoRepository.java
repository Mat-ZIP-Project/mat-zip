package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.mvc.domain.OwnerInfo;

import java.util.Optional;

public interface OwnerInfoRepository extends JpaRepository<OwnerInfo, Long> {
    /** 사업자 등록번호 중복체크 */
    boolean existsByBusinessNumber(String businessNumber);
    /** 사용자ID로 사업자ID 조회 */
    Optional<OwnerInfo> findByUser_Id(Long userId);
}
