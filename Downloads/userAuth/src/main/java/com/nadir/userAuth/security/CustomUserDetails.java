package com.nadir.userAuth.security;

import com.nadir.userAuth.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getUsername(); // 👈 Important
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // 👈 Important
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // You can implement roles later if needed
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
