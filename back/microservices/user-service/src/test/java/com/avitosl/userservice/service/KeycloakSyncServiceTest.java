package com.avitosl.userservice.service;

import com.avitosl.userservice.entity.Role;
import com.avitosl.userservice.entity.User;
import com.avitosl.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakSyncServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private KeycloakSyncService keycloakSyncService;

    private UserRepresentation kcUser1;
    private UserRepresentation kcUser2;
    private User localUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");

        kcUser1 = new UserRepresentation();
        kcUser1.setId("kc-1");
        kcUser1.setEmail("existing@example.com");
        kcUser1.setFirstName("Existing");
        kcUser1.setLastName("User");
        kcUser1.setEnabled(true);

        kcUser2 = new UserRepresentation();
        kcUser2.setId("kc-2");
        kcUser2.setEmail("new@example.com");
        kcUser2.setFirstName("New");
        kcUser2.setLastName("User");
        kcUser2.setEnabled(true);

        localUser = new User();
        localUser.setId(1L);
        localUser.setEmail("existing@example.com");
    }

    @Test
    void syncUsersFromKeycloak_ShouldSkipExistingUsersAndCreateNewOnes() {
        // Given
        when(keycloakService.getAllUsers()).thenReturn(Arrays.asList(kcUser1, kcUser2));
        when(roleService.getRoleByName("USER")).thenReturn(userRole);
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(localUser));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int syncedCount = keycloakSyncService.syncUsersFromKeycloak();

        // Then
        assertEquals(1, syncedCount);
        verify(userRepository).save(argThat(user ->
                "new@example.com".equals(user.getEmail()) &&
                        "New".equals(user.getFirstName())
        ));
    }

    @Test
    void syncUsersFromKeycloak_ShouldReturnZero_WhenAllUsersAlreadyExist() {
        // Given
        when(keycloakService.getAllUsers()).thenReturn(Arrays.asList(kcUser1));
        when(roleService.getRoleByName("USER")).thenReturn(userRole);
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(localUser));

        // When
        int syncedCount = keycloakSyncService.syncUsersFromKeycloak();

        // Then
        assertEquals(0, syncedCount);
        verify(userRepository, never()).save(any());
    }

    @Test
    void syncUsersFromKeycloak_ShouldHandleExceptions_AndContinueProcessing() {
        // Given
        UserRepresentation badKcUser = new UserRepresentation();
        badKcUser.setId("kc-bad");
        badKcUser.setEmail("bad@example.com");

        when(keycloakService.getAllUsers()).thenReturn(Arrays.asList(badKcUser, kcUser2));
        when(roleService.getRoleByName("USER")).thenReturn(userRole);
        when(userRepository.findByEmail("bad@example.com")).thenThrow(new RuntimeException("DB error"));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int syncedCount = keycloakSyncService.syncUsersFromKeycloak();

        // Then
        assertEquals(1, syncedCount);
        // Should have attempted both
        verify(userRepository, times(2)).findByEmail(anyString());
    }

    @Test
    void syncUsersFromKeycloak_ShouldSetKeycloakIdAndRoles() {
        // Given
        when(keycloakService.getAllUsers()).thenReturn(Arrays.asList(kcUser2));
        when(roleService.getRoleByName("USER")).thenReturn(userRole);
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            assertThat(saved.getKeycloakId()).isEqualTo("kc-2");
            assertThat(saved.getRoles()).contains(userRole);
            saved.setId(10L);
            return saved;
        });

        // When
        int syncedCount = keycloakSyncService.syncUsersFromKeycloak();

        // Then
        assertEquals(1, syncedCount);
    }
}
