package com.example.avito.service;

import com.example.avito.entity.Role;
import com.example.avito.entity.User;
import com.example.avito.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Роль " + name + " не найдена"));
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    public void assignRoleToUser(User user, Role role) {
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
        }
    }

    public void removeRoleFromUser(User user, Role role) {
        user.getRoles().remove(role);
    }
}