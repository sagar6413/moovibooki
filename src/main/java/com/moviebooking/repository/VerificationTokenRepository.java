package com.moviebooking.repository;

import com.moviebooking.model.entity.User;
import com.moviebooking.model.entity.VerificationToken;
import com.moviebooking.model.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<com.moviebooking.model.entity.VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserAndTokenType(User user, TokenType tokenType);

    @Query("SELECT t FROM VerificationToken t WHERE t.expiryDate < ?1 AND t.confirmedAt IS NULL")
    List<VerificationToken> findAllExpiredTokens(LocalDateTime now);

    void deleteByUser(User user);

    @Query("SELECT t FROM VerificationToken t LEFT JOIN FETCH t.user WHERE t.token = :token")
    Optional<VerificationToken> findByTokenWithUser(@Param("token") String token);

    void deleteByUserAndTokenType(User user, TokenType tokenType);
}