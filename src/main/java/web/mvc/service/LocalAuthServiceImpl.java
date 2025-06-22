package web.mvc.service;

import com.querydsl.core.types.Projections;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;


import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import web.mvc.domain.QUserLocalAuth;

import web.mvc.domain.User;
import web.mvc.domain.UserLocalAuth;
import web.mvc.dto.AuthLogDTO;
import web.mvc.dto.ResAuthDTO;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.LocalAuthRepository;


import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalAuthServiceImpl implements LocalAuthService {
    private final JPAQueryFactory jpaQueryFactory;
    private final LocalAuthRepository localAuthRepository;
    private final ModelMapper modelMapper;

    /**
     * 인증하기 (JPA 기본)
     */
    @Override
    public String insertAuthLog(AuthLogDTO authLogDTO) {
        QUserLocalAuth localAuth = QUserLocalAuth.userLocalAuth;

        UserLocalAuth entity = modelMapper.map(authLogDTO, UserLocalAuth.class);
                entity.setUser(User.builder().id(authLogDTO.getId()).build());
        try {
            UserLocalAuth authRes = localAuthRepository.save(entity);


            if (authRes == null) {
                log.error("인증 insert 실패 : {}", authLogDTO);
                throw new BasicException(ErrorCode.FAILED_AUTH);
            }
        } catch (DataIntegrityViolationException e) {  //중복인증으로 인한 제약조건 위반시 발생하는 RuntimeException
            throw new BasicException(ErrorCode.DUPLICATE_DATE);
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
                      localAuth.user.count()
                ))
                .from(localAuth)
                .where(localAuth.user.id.eq(id))
                .groupBy(localAuth.regionName)
                .fetch();
        if(localAuths.size()==0) throw new BasicException(ErrorCode.NO_AUTH_LOGS);

        return localAuths;
    }



}
