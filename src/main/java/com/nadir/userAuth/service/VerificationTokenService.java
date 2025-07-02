package com.nadir.userAuth.service;

import com.nadir.userAuth.model.User;
import com.nadir.userAuth.repository.VerificationTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificationTokenService {

    private final VerificationTokenRepository tokenRepo;

    public VerificationTokenService(VerificationTokenRepository tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    @Transactional
    public void deleteTokensByUser(User user) {
        tokenRepo.deleteByUser(user);
    }
}
