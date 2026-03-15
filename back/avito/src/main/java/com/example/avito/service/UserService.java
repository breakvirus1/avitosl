package com.example.avito.service;

import com.example.avito.entity.Role;
import com.example.avito.entity.User;
import com.example.avito.exception.ConflictException;
import com.example.avito.exception.NotFoundException;
import com.example.avito.mapper.UserMapper;
import com.example.avito.repository.RoleRepository;
import com.example.avito.repository.UserRepository;
import com.example.avito.request.RegisterRequest;
import com.example.avito.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new NotFoundException("Роль USER не найдена"));
        user.getRoles().add(userRole);

        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public UserResponse updateUser(Long id, RegisterRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        user.setFirstName(request.getFirstName());
        user.setPhoneNumber(request.getPhoneNumber());

        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<UserResponse> getAllUsers() {
        return userMapper.toResponseList(userRepository.findAll());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return userMapper.toResponse(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public void updateKeycloakId(Long userId, String keycloakId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        user.setKeycloakId(keycloakId);
        userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}