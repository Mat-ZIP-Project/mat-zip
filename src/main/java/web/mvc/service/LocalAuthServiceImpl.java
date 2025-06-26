package web.mvc.service;

import com.querydsl.core.types.Projections;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;


import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.*;


import web.mvc.dto.AuthLogDTO;
import web.mvc.dto.ResAuthDTO;
import web.mvc.dto.ResBadgeDTO;
import web.mvc.exception.ErrorCode;

import web.mvc.exception.LocalAuthException;
import web.mvc.repository.LocalAuthRepository;
import web.mvc.repository.LocalBadgeRepository;


import java.time.LocalDate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalAuthServiceImpl implements LocalAuthService {
    private final JPAQueryFactory jpaQueryFactory;
    private final LocalAuthRepository localAuthRepository;
    private final LocalBadgeRepository localBadgeRepository;
    private final ModelMapper modelMapper;



    /**
     * 인증하기 (JPA 기본 + QueryDSL)
     */
    @Transactional
    @Override
    public String insertAuthLog(AuthLogDTO authLogDTO) {
        Long id = authLogDTO.getId();

        QUserLocalBadge localBadge = QUserLocalBadge.userLocalBadge;

        UserLocalAuth entity = modelMapper.map(authLogDTO, UserLocalAuth.class);
        entity.setUser(User.builder().id(id).build());
        entity.setRegionName(authLogDTO.getRegionName().trim());
        entity.setAuthDate(LocalDate.now());
        try {
            log.info("entity authDate = {}", entity.getAuthDate());
            UserLocalAuth authRes = localAuthRepository.save(entity);




            if (authRes == null) {
                log.error("인증 insert 실패 : {}", authLogDTO);
                throw new LocalAuthException(ErrorCode.FAILED_AUTH);
            }
        } catch (DataIntegrityViolationException e) {  //중복인증으로 인한 제약조건 위반시 발생하는 RuntimeException
            throw new LocalAuthException(ErrorCode.DUPLICATE_DATE);
        }

        List<ResBadgeDTO> badgeList = searchBadges(id);

        if(badgeList.size()!=0) {
            List<ResBadgeDTO> sameRegionList = badgeList.stream().filter(badge -> badge.getRegionName().equals(authLogDTO.getRegionName())).toList();
            if (sameRegionList.size() != 0) {// 로컬뱃지 지역일 때(local_badges 테이블에 해당 지역이 있다!)

                ResBadgeDTO badge = sameRegionList.get(0);

                // 기존 validUntil에 30일 더한 값 생성
                LocalDate plus30Days = badge.getValidUntil().plusDays(30);

                jpaQueryFactory.update(localBadge)  // 인증유효기간 + 30일 update 한다.
                        .set(localBadge.validUntil, plus30Days)
                        .where(localBadge.regionName.eq(badge.getRegionName()).and(localBadge.user.id.eq(id)))
                        .execute();

                return "인증 유효일자가 갱신되었습니다.";
            }
        }

        List<ResAuthDTO> authLogList= searchAuthLogs(id);
        List<ResAuthDTO> resultList= authLogList.stream().filter(log->log.getRegionName().equals(authLogDTO.getRegionName())).toList();
        Long count = resultList.get(0).getAuthCount();
        log.info("count = {}", count);

        if(badgeList.size()<2 && count>=3 ) {// 로컬뱃지가 2개미만+ 30일 이내 인증횟수 3회 이상 일때
            localBadgeRepository.save(UserLocalBadge.builder().user(User.builder().id(id).build()).regionName(authLogDTO.getRegionName()).build());// local_badges 테이블에 뱃지 추가
             // Users 테이블에 인증여부 1로 update
            return "현지인 인증뱃지가 발급되었습니다.";
        }


        return authLogDTO.getRegionName();
    }






    /**
     * 인증기록 검색 (QueryDSL)
     */
    @Override
    public List<ResAuthDTO> searchAuthLogs( Long id) {
        QUserLocalAuth localAuth = QUserLocalAuth.userLocalAuth;
        List<ResAuthDTO> localAuths = jpaQueryFactory
                .select(Projections.constructor(ResAuthDTO.class,
                      localAuth.regionName,
                      localAuth.count()
                ))
                .from(localAuth)
                .where(localAuth.user.id.eq(id).and(localAuth.authDate.between(LocalDate.now().minusDays(30), LocalDate.now())))
                .groupBy(localAuth.regionName)
                .fetch();
        if(localAuths.size()==0) throw new LocalAuthException(ErrorCode.NO_AUTH_LOGS);

        return localAuths;
    }
    /**
     * 로컬 인증 뱃지 검색 (QueryDSL)
     */
    @Override
    public List<ResBadgeDTO> searchBadges(Long id) {

        QUserLocalBadge localBadge = QUserLocalBadge.userLocalBadge;
        List<ResBadgeDTO> localBadges = jpaQueryFactory
                .select(Projections.constructor(ResBadgeDTO.class,
                        localBadge.authId,
                        localBadge.regionName,
                        localBadge.validUntil
                ))
                .from(localBadge)
                .where(localBadge.user.id.eq(id))
                .fetch();

        return localBadges;
    }



    /**
     * 인증 뱃지 만료 처리(SpringScheduler + QueryDSL + 기본 JPA)
     */
    @Override
    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // 매일 새벽 0시 실행
    public void deleteExpiredBadges() {
         //1. 만료된 뱃지 검색
        QUserLocalAuth localAuth = QUserLocalAuth.userLocalAuth;
        QUserLocalBadge localBadge = QUserLocalBadge.userLocalBadge;

        List<UserLocalBadge> expiredBadges= jpaQueryFactory
                .selectFrom(localBadge)
                .where(localBadge.validUntil.before(LocalDate.now()))
                .fetch();


         //2. 만료된 뱃지 & 인증로그 삭제
        for (UserLocalBadge badge : expiredBadges) {
            localBadgeRepository.delete(badge); ;
            jpaQueryFactory.delete(localAuth).where(localAuth.regionName.eq(badge.getRegionName()).and(localAuth.user.id.eq(badge.getUser().getId()))).execute();
        }
    }

    /**
     * 로컬 인증 뱃지 삭제하기
     */
    @Transactional
    @Override
    public String deleteBadges(Long badgeId,Long id) {
        QUserLocalAuth localAuth = QUserLocalAuth.userLocalAuth;

        UserLocalBadge badge=localBadgeRepository.findById(badgeId).get();

        jpaQueryFactory.delete(localAuth).where(localAuth.regionName.eq(badge.getRegionName()).and(localAuth.user.id.eq(id))).execute();
        localBadgeRepository.delete(badge);


        return badge.getRegionName()+" 인증 뱃지가 삭제되었습니다.";
    }



}
