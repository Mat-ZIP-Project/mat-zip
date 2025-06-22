package web.mvc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Repository;
import web.mvc.domain.User;
import web.mvc.domain.UserLocalAuth;
import web.mvc.repository.LocalAuthRepository;
import web.mvc.service.LocalAuthService;

import java.time.LocalDate;

@SpringBootTest
class MatzipApplicationTests {
    @Autowired
    private LocalAuthRepository localAuthRepository;

    @Test
    void contextLoads() {

    }

    @Test
    void insertLocalAuth(){
        localAuthRepository.save(UserLocalAuth.builder().regionName("강남구").user(User.builder().id(1L).build()).authDate(LocalDate.of(2025,6,22)).build());
    }

}
