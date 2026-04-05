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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest("John", "test@example.com", "password", "password", "1234567890");
        Role userRole = new Role();
        userRole.setName("USER");
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setPassword("encodedPassword");
        user.setEnabled(true);
        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setEmail("test@example.com");
        expectedResponse.setFirstName("John");

        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        lenient().when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        lenient().when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        lenient().when(userRepository.save(any(User.class))).thenReturn(user);
        lenient().when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        // When
        UserResponse result = userService.createUser(request);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowConflictExceptionWhenEmailExists() {
        // Given
        RegisterRequest request = new RegisterRequest("John", "existing@example.com", "pass", "pass", null);
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Пользователь с таким email уже существует");
    }

    @Test
    void createUser_shouldThrowNotFoundExceptionWhenRoleNotFound() {
        // Given
        RegisterRequest request = new RegisterRequest("John", "test@example.com", "pass", "pass", null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Роль USER не найдена");
    }

    @Test
    void findByEmail_shouldReturnUser() {
        // Given
        User user = new User();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenNotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldReturnUser() {
        // Given
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findById(1L);

        // Then
        assertThat(result).isPresent();
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void updateUser_shouldUpdateUser() {
        // Given
        RegisterRequest request = new RegisterRequest("Updated", null, null, null, "1234567890");
        User author = new User();
        User existingUser = new User();
        existingUser.setId(1L);
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setFirstName("Updated");
        updatedUser.setPhoneNumber("1234567890");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);

        // When
        userService.updateUser(1L, request);

        // Then
        verify(userRepository).save(existingUser);
        assertThat(existingUser.getFirstName()).isEqualTo("Updated");
        assertThat(existingUser.getPhoneNumber()).isEqualTo("1234567890");
    }

    @Test
    void updateUser_shouldThrowNotFoundExceptionWhenUserNotFound() {
        // Given
        RegisterRequest request = new RegisterRequest("Updated", null, null, null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    void deleteUser_shouldDisableUser() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEnabled(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).save(user);
        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void deleteUser_shouldThrowNotFoundExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    void createUser_shouldHandleNullRequest() {
        // When & Then
        assertThatThrownBy(() -> userService.createUser(null))
                .isInstanceOf(NullPointerException.class);
    }
}