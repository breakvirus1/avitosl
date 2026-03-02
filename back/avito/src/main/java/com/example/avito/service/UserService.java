package com.example.avito.service;

import com.example.avito.dto.response.UserResponse;
import com.example.avito.entity.User;
import com.example.avito.mapper.UserMapper;
import com.example.avito.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserMapper userMapper;

    public UserResponse getCurrentUser() {
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) request.getUserPrincipal();
        KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
        KeycloakSecurityContext session = principal.getKeycloakSecurityContext();
        AccessToken accessToken = session.getToken();
        String email = accessToken.getEmail();

        User user = userRepository.findByEmail(email);

        return userMapper.toResponse(user);

    }

    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email);

        return userMapper.toResponse(user);

    }
}