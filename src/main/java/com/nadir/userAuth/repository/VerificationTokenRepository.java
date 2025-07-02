package com.nadir.userAuth.repository;

import jakarta.transaction.Transactional;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.nadir.userAuth.model.User;
import com.nadir.userAuth.model.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    // ✅ Required to fetch token by user
    Optional<VerificationToken> findByUser(User user);

    @Modifying
    @Transactional
    void deleteByUser(User user);
}
