package com.avitosl.userservice.service;

import com.avitosl.userservice.entity.Role;
import com.avitosl.userservice.entity.User;
import com.avitosl.userservice.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ADMIN");
    }

    @Test
    void getOrCreateRole_ShouldReturnExistingRole_WhenRoleExists() {
        // Given
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));

        // When
        Role result = roleService.getOrCreateRole("USER");

        // Then
        assertThat(result).isEqualTo(userRole);
        verify(roleRepository).findByName("USER");
        verify(roleRepository, never()).save(any());
    }

    @Test
    void getOrCreateRole_ShouldCreateAndReturnRole_WhenRoleDoesNotExist() {
        // Given
        when(roleRepository.findByName("NEW_ROLE")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role r = invocation.getArgument(0);
            r.setId(3L);
            return r;
        });

        // When
        Role result = roleService.getOrCreateRole("NEW_ROLE");

        // Then
        assertThat(result).isNotNull();
        assertEquals("NEW_ROLE", result.getName());
        verify(roleRepository).findByName("NEW_ROLE");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void getDefaultRoles_ShouldReturnUserAndAdminRoles() {
        // Given
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

        // When
        Set<Role> result = roleService.getDefaultRoles();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(userRole, adminRole);
        verify(roleRepository, times(2)).findByName(anyString());
    }

    @Test
    void getRoleByName_ShouldReturnRole_WhenExists() {
        // Given
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

        // When
        Role result = roleService.getRoleByName("ADMIN");

        // Then
        assertThat(result).isEqualTo(adminRole);
    }

    @Test
    void getRoleByName_ShouldThrowRuntimeException_WhenRoleNotFound() {
        // Given
        when(roleRepository.findByName("UNKNOWN")).thenReturn(Optional.empty());

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.getRoleByName("UNKNOWN");
        });
        assertEquals("Role not found: UNKNOWN", exception.getMessage());
    }

    @Test
    void assignRoleToUser_ShouldAddRole_WhenUserDoesNotHaveIt() {
        // Given
        User user = new User();
        user.setRoles(new HashSet<>());
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        // When
        roleService.assignRoleToUser(user, role);

        // Then
        assertThat(user.getRoles()).contains(role);
    }

    @Test
    void assignRoleToUser_ShouldNotAddDuplicateRole_WhenUserAlreadyHasIt() {
        // Given
        User user = new User();
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        roles.add(role);
        user.setRoles(roles);

        // When
        roleService.assignRoleToUser(user, role);

        // Then
        assertThat(user.getRoles()).hasSize(1);
    }

    @Test
    void removeRoleFromUser_ShouldRemoveRole() {
        // Given
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        User user = new User();
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        // When
        roleService.removeRoleFromUser(user, role);

        // Then
        assertThat(user.getRoles()).doesNotContain(role);
    }
}
