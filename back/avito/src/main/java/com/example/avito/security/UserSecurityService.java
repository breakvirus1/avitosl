package com.example.avito.security;

import com.example.avito.entity.User;
import com.example.avito.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSecurityService {

    private final UserRepository userRepository;

    public UserInfo getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Пользователь не аутентифицирован");
        }
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getClaimAsString("email");
        String keycloakId = jwt.getClaimAsString("sub");
        String firstName = jwt.getClaimAsString("given_name");
        if (firstName == null || firstName.isEmpty()) {
            firstName = email.split("@")[0];
        }
        
        return new UserInfo(email, keycloakId, firstName);
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Пользователь не аутентифицирован");
        }
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaimAsString("email");
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Пользователь не аутентифицирован");
        }
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getClaimAsString("email");
        
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Пользователь не найден в базе данных");
        }
        
        return user.get();
    }

    public static class UserInfo {
        private final String email;
        private final String keycloakId;
        private final String firstName;
        
        public UserInfo(String email, String keycloakId, String firstName) {
            this.email = email;
            this.keycloakId = keycloakId;
            this.firstName = firstName;
        }
        
        public String getEmail() { return email; }
        public String getKeycloakId() { return keycloakId; }
        public String getFirstName() { return firstName; }
    }
}