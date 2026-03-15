package com.example.avito.service;

import com.example.avito.entity.*;
import com.example.avito.repository.*;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class FakeDataGeneratorService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PhotoRepository photoRepository;
    private final RoleService roleService;
    private final KeycloakService keycloakService;

    private final Faker faker = new Faker(new Locale("ru"));
    private final Random random = new Random();

    @Transactional
    public void generateFakePosts(int count) {
        // Синхронизируем пользователей из Keycloak если их нет в базе
        syncUsersFromKeycloakIfNeeded();
        
        List<User> users = userRepository.findAll();
        List<Category> categories = categoryRepository.findAll();
        List<Subcategory> allSubcategories = subcategoryRepository.findAll();

        if (users.isEmpty()) {
            throw new IllegalStateException("Нет пользователей в базе данных. Не удалось синхронизировать из Keycloak. Убедитесь, что Keycloak запущен и в нем есть пользователи.");
        }

        if (categories.isEmpty() || allSubcategories.isEmpty()) {
            throw new IllegalStateException("Нет категорий или подкатегорий в базе данных. Сначала создайте категории и подкатегории.");
        }

        for (int i = 0; i < count; i++) {
            User randomUser = users.get(random.nextInt(users.size()));
            Category randomCategory = categories.get(random.nextInt(categories.size()));
            
            // Получаем подкатегории для выбранной категории
            List<Subcategory> categorySubcategories = allSubcategories.stream()
                    .filter(sc -> sc.getCategory().getId().equals(randomCategory.getId()))
                    .toList();
            
            if (categorySubcategories.isEmpty()) {
                // Если у категории нет подкатегорий, пропускаем или берем любую подкатегорию
                continue;
            }
            
            Subcategory randomSubcategory = categorySubcategories.get(random.nextInt(categorySubcategories.size()));

            Post post = Post.builder()
                    .title(faker.commerce().productName())
                    .description(faker.lorem().paragraph(2))
                    .price(BigDecimal.valueOf(faker.number().randomDouble(2, 100, 100000)))
                    .author(randomUser)
                    .category(randomCategory)
                    .subcategory(randomSubcategory)
                    .active(true)
                    .createdAt(LocalDateTime.now().minusDays(random.nextInt(30)))
                    .updatedAt(LocalDateTime.now())
                    .build();

            Post savedPost = postRepository.save(post);

            // Добавляем случайное количество комментариев (0-5)
            int commentCount = random.nextInt(6);
            for (int j = 0; j < commentCount; j++) {
                User commentAuthor = users.get(random.nextInt(users.size()));
                Comment comment = Comment.builder()
                        .text(faker.lorem().sentence())
                        .author(commentAuthor)
                        .post(savedPost)
                        .createdAt(LocalDateTime.now().minusDays(random.nextInt(10)))
                        .updatedAt(LocalDateTime.now())
                        .build();
                commentRepository.save(comment);
            }
        }
    }

    @Transactional
    public void clearAllPosts() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
    }
    
    /**
     * Синхронизирует пользователей из Keycloak в локальную базу данных.
     * Вызывается только если в базе нет пользователей.
     */
    private void syncUsersFromKeycloakIfNeeded() {
        if (userRepository.count() > 0) {
            return; // Пользователи уже есть, синхронизация не нужна
        }
        
        try {
            List<UserRepresentation> keycloakUsers = keycloakService.getAllUsers();
            Role userRole = roleService.getRoleByName("USER");
            
            int syncedCount = 0;
            for (UserRepresentation kcUser : keycloakUsers) {
                try {
                    if (userRepository.findByEmail(kcUser.getEmail()).isPresent()) {
                        continue;
                    }

                    User user = User.builder()
                            .email(kcUser.getEmail())
                            .firstName(kcUser.getFirstName())
                            .phoneNumber(kcUser.getAttributes() != null && kcUser.getAttributes().containsKey("phone")
                                ? kcUser.getAttributes().get("phone").get(0)
                                : null)
                            .keycloakId(kcUser.getId())
                            .enabled(kcUser.isEnabled())
                            .password("KEYCLOAK_AUTH") // Dummy password for Keycloak-synced users
                            .build();
                    
                    user.getRoles().add(userRole);
                    userRepository.save(user);
                    syncedCount++;
                } catch (Exception e) {
                    // Пропускаем пользователя при ошибке
                    System.err.println("Ошибка синхронизации пользователя " + kcUser.getEmail() + ": " + e.getMessage());
                }
            }
            
            if (syncedCount > 0) {
                System.out.println("✅ Синхронизировано " + syncedCount + " пользователей из Keycloak");
            } else {
                System.out.println("⚠️ Не найдено пользователей в Keycloak для синхронизации. Убедитесь, что Keycloak запущен и содержит пользователей.");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Не удалось синхронизировать пользователей из Keycloak: " + e.getMessage());
        }
    }
}
