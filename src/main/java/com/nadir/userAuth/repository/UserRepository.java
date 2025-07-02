package com.nadir.userAuth.repository;

import com.nadir.userAuth.model.User;

//import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
//     findByVerificationToken(String token);

}
