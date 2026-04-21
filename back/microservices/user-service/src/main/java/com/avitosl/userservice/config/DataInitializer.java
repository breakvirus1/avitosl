package com.avitosl.userservice.config;

import com.avitosl.userservice.entity.Role;
import com.avitosl.userservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        createRolesIfNotFound();
    }

    private void createRolesIfNotFound() {
        Set<String> roleNames = Set.of("USER", "ADMIN");

        for (String roleName : roleNames) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                return role;
            });
        }
    }
}
