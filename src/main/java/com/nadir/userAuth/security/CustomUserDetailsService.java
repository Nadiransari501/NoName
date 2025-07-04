package com.nadir.userAuth.security;

import com.nadir.userAuth.model.User;
import com.nadir.userAuth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username); // ✅ Login by username
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername()) // ✅ authentication.getName() will return this
                .password(user.getPassword())
                .authorities("USER")
                .accountLocked(false)
                .disabled(!user.isEnabled())
                .build();
    }
}
