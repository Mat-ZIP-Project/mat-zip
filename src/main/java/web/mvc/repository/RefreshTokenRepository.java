package web.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.mvc.domain.RefreshToken;
import web.mvc.domain.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.expiresAt > :now")
    Optional<RefreshToken> findByTokenAndNotExpired(@Param("token") String token, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    void deleteByUser(User user);
}
