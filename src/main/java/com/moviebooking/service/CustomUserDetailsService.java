package com.moviebooking.service;

import com.moviebooking.exception.CustomExceptions;
import com.moviebooking.model.entity.User;
import com.moviebooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                                  .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found: " + usernameOrEmail));
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                                                .collect(Collectors.toSet());
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), user.isEnabled(), true, true, true, authorities);
    }
}