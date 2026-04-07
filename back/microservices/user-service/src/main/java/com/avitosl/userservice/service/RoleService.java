package com.avitosl.userservice.service;

import com.avitosl.userservice.entity.Role;
import com.avitosl.userservice.entity.User;
import com.avitosl.userservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return roleRepository.save(role);
                });
    }

    public Set<Role> getDefaultRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(getOrCreateRole("USER"));
        roles.add(getOrCreateRole("ADMIN"));
        return roles;
    }

    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Role not found: " + name));
    }
}
