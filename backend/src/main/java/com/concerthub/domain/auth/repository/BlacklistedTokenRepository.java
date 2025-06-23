package com.concerthub.domain.auth.repository;

import com.concerthub.domain.auth.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    boolean existsByToken(String token);

    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
