package com.avitosl.userservice.service;

import com.avitosl.userservice.entity.User;
import com.avitosl.userservice.exception.ConflictException;
import com.avitosl.userservice.exception.NotFoundException;
import com.avitosl.userservice.mapper.UserMapper;
import com.avitosl.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<Long> idCaptor;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPasswordHash("encodedPassword1");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setPhoneNumber("+1234567890");
        user1.setWalletBalance(100.0);
        user1.setEnabled(true);
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("encodedPassword2");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setPhoneNumber("+0987654321");
        user2.setWalletBalance(200.0);
        user2.setEnabled(true);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createUser_ShouldSaveUserAndReturnIt_WhenUsernameAndEmailAreUnique() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(3L);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            return user;
        });

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setPasswordHash("encodedPass");
        newUser.setFirstName("New");
        newUser.setLastName("User");

        // When
        User result = userService.createUser(newUser);

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("newuser@example.com", result.getEmail());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowConflictException_WhenUsernameExists() {
        // Given
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        User user = new User();
        user.setUsername("existinguser");

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            userService.createUser(user);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowConflictException_WhenEmailExists() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        User user = new User();
        user.setUsername("newuser");
        user.setEmail("existing@example.com");

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            userService.createUser(user);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        // When
        User result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("user1", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getUserById(999L);
        });

        assertThat(exception.getMessage()).contains("User not found");
        verify(userRepository).findById(999L);
    }

    @Test
    void getUserByUsername_ShouldReturnUser_WhenExists() {
        // Given
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));

        // When
        User result = userService.getUserByUsername("user1");

        // Then
        assertNotNull(result);
        assertEquals("user1", result.getUsername());
        verify(userRepository).findByUsername("user1");
    }

    @Test
    void getUserByUsername_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getUserByUsername("nonexistent");
        });

        assertThat(exception.getMessage()).contains("User not found");
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void getUserByKeycloakId_ShouldReturnUser_WhenExists() {
        // Given
        when(userRepository.findByKeycloakId("kc123")).thenReturn(Optional.of(user1));

        // When
        User result = userService.getUserByKeycloakId("kc123");

        // Then
        assertNotNull(result);
        assertEquals("user1", result.getUsername());
        verify(userRepository).findByKeycloakId("kc123");
    }

    @Test
    void getUserByKeycloakId_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(userRepository.findByKeycloakId("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getUserByKeycloakId("nonexistent");
        });

        assertThat(exception.getMessage()).contains("User not found");
        verify(userRepository).findByKeycloakId("nonexistent");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser_WhenIdExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updateDetails = new User();
        updateDetails.setUsername("updatedUser");
        updateDetails.setEmail("updated@example.com");
        updateDetails.setFirstName("Updated");
        updateDetails.setLastName("Name");
        updateDetails.setPhoneNumber("+1111111111");

        // When
        User result = userService.updateUser(1L, updateDetails);

        // Then
        assertNotNull(result);
        assertEquals("updatedUser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowNotFoundException_WhenIdDoesNotExist() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User updateDetails = new User();
        updateDetails.setUsername("updated");

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.updateUser(999L, updateDetails);
        });

        assertThat(exception.getMessage()).contains("User not found");
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        doNothing().when(userRepository).delete(any(User.class));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(user1);
    }

    @Test
    void deleteUser_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.deleteUser(999L);
        });

        assertThat(exception.getMessage()).contains("User not found");
        verify(userRepository).findById(999L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void addFundsToWallet_ShouldIncreaseBalance_WhenAmountIsPositive() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.addFundsToWallet(1L, 50.0);

        // Then
        assertNotNull(result);
        assertEquals(150.0, result.getWalletBalance());
        verify(userRepository).findById(1L);
        verify(userRepository).save(user1);
    }

    @Test
    void addFundsToWallet_ShouldThrowIllegalArgumentException_WhenAmountIsNull() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addFundsToWallet(1L, null);
        });

        assertEquals("Amount must be positive", exception.getMessage());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void addFundsToWallet_ShouldThrowIllegalArgumentException_WhenAmountIsNegative() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addFundsToWallet(1L, -10.0);
        });

        assertEquals("Amount must be positive", exception.getMessage());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void addFundsToWallet_ShouldThrowIllegalArgumentException_WhenAmountIsZero() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addFundsToWallet(1L, 0.0);
        });

        assertEquals("Amount must be positive", exception.getMessage());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void subtractFromWallet_ShouldDecreaseBalance_WhenAmountIsPositiveAndSufficientFunds() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.subtractFromWallet(1L, 30.0);

        // Then
        assertNotNull(result);
        assertEquals(70.0, result.getWalletBalance());
        verify(userRepository).findById(1L);
        verify(userRepository).save(user1);
    }

    @Test
    void subtractFromWallet_ShouldThrowIllegalArgumentException_WhenAmountIsNegative() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.subtractFromWallet(1L, -10.0);
        });

        assertEquals("Amount must be positive", exception.getMessage());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void subtractFromWallet_ShouldThrowRuntimeException_WhenInsufficientFunds() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.subtractFromWallet(1L, 500.0);
        });

        assertEquals("Insufficient funds", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void subtractFromWallet_ShouldThrowIllegalArgumentException_WhenAmountIsZero() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.subtractFromWallet(1L, 0.0);
        });

        assertEquals("Amount must be positive", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }
}
