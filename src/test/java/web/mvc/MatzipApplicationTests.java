package web.mvc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Repository;
import web.mvc.domain.User;
import web.mvc.domain.UserLocalAuth;
import web.mvc.domain.UserLocalBadge;
import web.mvc.repository.LocalAuthRepository;
import web.mvc.repository.LocalBadgeRepository;
import web.mvc.service.LocalAuthService;

import java.time.LocalDate;

@SpringBootTest
class MatzipApplicationTests {
    @Autowired
    private LocalAuthRepository localAuthRepository;
    @Autowired
    private LocalBadgeRepository badgeRepository;

    @Test
    void contextLoads() {

    }

    @Test
    void insertLocalAuth(){
        localAuthRepository.save(UserLocalAuth.builder().regionName("경기도 용인시 수지구").user(User.builder().id(7L).build()).authDate(LocalDate.of(2025,6,19)).build());
        localAuthRepository.save(UserLocalAuth.builder().regionName("경기도 용인시 수지구").user(User.builder().id(7L).build()).authDate(LocalDate.of(2025,6,20)).build());
    }
    @Test
    void insertBadge(){
        badgeRepository.save(UserLocalBadge.builder().regionName("서울특별시 강남구").user(User.builder().id(10L).build()).build());
    }


}
