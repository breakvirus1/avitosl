package com.avitosl.userservice.service;

import com.avitosl.userservice.entity.Role;
import com.avitosl.userservice.entity.User;
import com.avitosl.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakSyncService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final RoleService roleService;

    /**
     * Синхронизирует пользователей из Keycloak в локальную базу данных.
     * Получает всех пользователей из Keycloak и добавляет их в локальную базу,
     * если они еще не существуют.
     */
    @Transactional
    public int syncUsersFromKeycloak() {
        List<UserRepresentation> keycloakUsers = keycloakService.getAllUsers();
        Role userRole = roleService.getRoleByName("USER");

        int syncedCount = 0;
        for (UserRepresentation kcUser : keycloakUsers) {
            try {
                // Проверяем, существует ли пользователь с таким email в базе
                if (userRepository.findByEmail(kcUser.getEmail()).isPresent()) {
                    continue; // Пропускаем, уже есть
                }

                // Создаем нового пользователя из данных Keycloak
                User user = new User();
                user.setEmail(kcUser.getEmail());
                user.setFirstName(kcUser.getFirstName());
                user.setPhoneNumber(kcUser.getAttributes() != null && kcUser.getAttributes().containsKey("phone")
                    ? kcUser.getAttributes().get("phone").get(0)
                    : null);
                user.setKeycloakId(kcUser.getId());
                user.setEnabled(kcUser.isEnabled());
                user.setPasswordHash("KEYCLOAK_AUTH"); // Dummy password hash for Keycloak-synced users
                user.setUsername(kcUser.getEmail()); // Assuming username is email

                user.getRoles().add(userRole);
                userRepository.save(user);
                syncedCount++;
            } catch (Exception e) {
                // Пропускаем пользователя при ошибке
                System.err.println("Ошибка синхронизации пользователя " + kcUser.getEmail() + ": " + e.getMessage());
            }
        }

        return syncedCount;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔄 Запуск синхронизации пользователей из Keycloak...");
        int attempts = 0;
        int maxAttempts = 10;
        long delayMs = 5000;

        while (attempts < maxAttempts) {
            try {
                int synced = syncUsersFromKeycloak();
                System.out.println("✅ Синхронизировано " + synced + " пользователей");
                // Sync attempt succeeded (even if zero new users), break loop
                break;
            } catch (Exception e) {
                attempts++;
                System.err.println("⚠️ Ошибка синхронизации (попытка " + attempts + "): " + e.getMessage());
                if (attempts >= maxAttempts) {
                    System.err.println("❌ Не удалось синхронизировать после " + maxAttempts + " попыток.");
                    break;
                }
                Thread.sleep(delayMs);
            }
        }

        System.out.println("📊 Всего пользователей в базе: " + userRepository.count());
        System.out.println("✅ Синхронизация завершена");
    }
}
