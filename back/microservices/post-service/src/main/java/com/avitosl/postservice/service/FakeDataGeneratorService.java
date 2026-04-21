package com.avitosl.postservice.service;

import com.avitosl.postservice.entity.Comment;
import com.avitosl.postservice.entity.Photo;
import com.avitosl.postservice.entity.Post;
import com.avitosl.postservice.feign.CategoryServiceClient;
import com.avitosl.postservice.feign.ChatServiceClient;
import com.avitosl.postservice.feign.SubcategoryServiceClient;
import com.avitosl.postservice.feign.UserServiceClient;
import com.avitosl.postservice.repository.CommentRepository;
import com.avitosl.postservice.repository.PhotoRepository;
import com.avitosl.postservice.repository.PostRepository;
import com.avitosl.postservice.request.CategoryCreateRequest;
import com.avitosl.postservice.request.ChatMessageRequest;
import com.avitosl.postservice.request.SubcategoryCreateRequest;
import com.avitosl.postservice.response.CategoryResponse;
import com.avitosl.postservice.response.ChatMessageResponse;
import com.avitosl.postservice.response.SubcategoryResponse;
import com.avitosl.postservice.response.UserResponse;
import com.github.javafaker.Faker;
import feign.FeignException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FakeDataGeneratorService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PhotoRepository photoRepository;
    private final UserServiceClient userServiceClient;
    private final CategoryServiceClient categoryServiceClient;
    private final SubcategoryServiceClient subcategoryServiceClient;
    private final ChatServiceClient chatServiceClient;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private final Faker faker = new Faker(new Locale("ru"));
    private final Random random = new Random();
    
    // A tiny 1x1 black JPEG image (base64) used as default photo content
    private static final byte[] DEFAULT_IMAGE = Base64.getDecoder().decode(
        "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/wAALCAABAAEBAREA/8QAFAABAAAAAAAAAAAAAAAAAAAACf/EABQQAQAAAAAAAAAAAAAAAAAAAAD/2gAIAQEAAD8AVN//2Q=="
    );
    
    // Retry configuration
    private static final int MAX_RETRIES = 10;
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds

    @Transactional
    public List<Post> generateFakePosts(int count) {
        List<UserResponse> users;
        try {
            users = userServiceClient.getAllUsers();
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось получить пользователей из UserService: " + e.getMessage(), e);
        }
        if (users.isEmpty()) {
            throw new IllegalStateException("Нет пользователей в базе данных.");
        }

        // Ensure categories exist with retry
        ensureCategoriesWithRetry();
        
        List<CategoryResponse> categories;
        try {
            categories = retryFeignCall(() -> categoryServiceClient.getAllCategories());
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось получить категории: " + e.getMessage(), e);
        }
        if (categories == null || categories.isEmpty()) {
            throw new IllegalStateException("Не удалось создать категории.");
        }

        // Ensure subcategories exist and get map with retry
        Map<Long, List<SubcategoryResponse>> subcatsByCategory;
        try {
            subcatsByCategory = ensureSubcategoriesWithRetry();
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось создать подкатегории: " + e.getMessage(), e);
        }

        // Generate posts
        for (int i = 0; i < count; i++) {
            UserResponse randomUser = users.get(random.nextInt(users.size()));
            CategoryResponse randomCategory = categories.get(random.nextInt(categories.size()));
            List<SubcategoryResponse> subcats = subcatsByCategory.get(randomCategory.getId());
            if (subcats == null || subcats.isEmpty()) {
                continue; // skip if no subcategories
            }
            SubcategoryResponse randomSubcat = subcats.get(random.nextInt(subcats.size()));

            Post post = new Post();
            post.setTitle(faker.commerce().productName());
            post.setDescription(faker.lorem().paragraph(2));
            post.setPrice(faker.number().randomDouble(2, 100, 100000));
            post.setKeycloakId(randomUser.getKeycloakId());
            post.setCategoryId(randomCategory.getId());
            post.setSubcategoryId(randomSubcat.getId());
            post.setIsActive(true);
            post.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
            post.setUpdatedAt(LocalDateTime.now());

            // Generate photos with actual image files
            int photoCount = random.nextInt(3) + 1; // 1-3 photos
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                System.err.println("Failed to create upload directory: " + e.getMessage());
            }
            for (int p = 0; p < photoCount; p++) {
                Photo photo = new Photo();
                photo.setPost(post);
                String fileName = UUID.randomUUID() + ".jpg";
                Path filePath = uploadPath.resolve(fileName);
                try {
                    Files.write(filePath, DEFAULT_IMAGE);
                    photo.setFilePath(filePath.toString());
                } catch (IOException e) {
                    System.err.println("Failed to write default image for photo: " + e.getMessage());
                    // Use fallback path; file won't exist but loadFile will provide default image
                    photo.setFilePath("/app/uploads/" + fileName);
                }
                photo.setFileName(fileName);
                photo.setFileSize((long) DEFAULT_IMAGE.length);
                photo.setContentType("image/jpeg");
                photo.setIsPrimary(p == 0);
                post.getPhotos().add(photo);
            }

            // Generate comments
            int commentCount = random.nextInt(5); // 0-4 comments
            for (int c = 0; c < commentCount; c++) {
                UserResponse commentUser = users.get(random.nextInt(users.size()));
                Comment comment = new Comment();
                comment.setPost(post);
                comment.setUserId(commentUser.getId());
                comment.setContent(faker.lorem().sentence());
                // Set author name from user data
                comment.setAuthorFirstName(commentUser.getFirstName() != null ? commentUser.getFirstName() : "Пользователь");
                comment.setAuthorLastName(commentUser.getLastName() != null ? commentUser.getLastName() : "");
                post.getComments().add(comment);
            }

            postRepository.save(post);
        }

        // Generate chat messages
        generateChatMessages(users);

        return postRepository.findAll();
    }

    private void ensureCategoriesWithRetry() {
        try {
            List<CategoryResponse> existing = retryFeignCall(() -> categoryServiceClient.getAllCategories());
            if (existing != null && !existing.isEmpty()) {
                return;
            }
        } catch (Exception e) {
            System.err.println("Не удалось получить категории, попытка создания: " + e.getMessage());
        }
        
        // Create default categories
        String[][] categoriesData = {
            {"Транспорт", "Транспортные средства"},
            {"Недвижимость", "Квартиры, дома, земля"},
            {"Услуги", "Различные услуги"},
            {"Электроника", "Техника и гаджеты"},
            {"Одежда", "Одежда и аксессуары"}
        };
        for (String[] data : categoriesData) {
            CategoryCreateRequest req = new CategoryCreateRequest();
            req.setName(data[0]);
            req.setDescription(data[1]);
            try {
                retryFeignCall(() -> {
                    categoryServiceClient.createCategory(req);
                    return null;
                });
            } catch (Exception e) {
                System.err.println("Ошибка создания категории " + data[0] + ": " + e.getMessage());
            }
        }
    }

    private Map<Long, List<SubcategoryResponse>> ensureSubcategoriesWithRetry() {
        List<CategoryResponse> categories;
        try {
            categories = retryFeignCall(() -> categoryServiceClient.getAllCategories());
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось получить категории для подкатегорий: " + e.getMessage(), e);
        }
        Map<Long, List<SubcategoryResponse>> result = new HashMap<>();

        for (CategoryResponse cat : categories) {
            List<SubcategoryResponse> subcats;
            try {
                subcats = retryFeignCall(() -> subcategoryServiceClient.getByCategory(cat.getId()));
            } catch (Exception e) {
                System.err.println("Не удалось получить подкатегории для категории " + cat.getName() + ": " + e.getMessage());
                subcats = Collections.emptyList();
            }
            if (subcats != null && !subcats.isEmpty()) {
                result.put(cat.getId(), subcats);
                continue;
            }

            String[] names;
            switch (cat.getName()) {
                case "Транспорт":
                    names = new String[]{"Автомобили", "Мотоциклы", "Запчасти", "Самокаты", "Велосипеды"};
                    break;
                case "Недвижимость":
                    names = new String[]{"Квартиры", "Дома", "Участки", "Коммерческая", "Аренда"};
                    break;
                case "Услуги":
                    names = new String[]{"Ремонт", "Красота", "Обучение", "Уборка", "Ремонт техники"};
                    break;
                case "Электроника":
                    names = new String[]{"Телефоны", "Ноутбуки", "Телевизоры", "Фотоаппараты", "Аксессуары"};
                    break;
                case "Одежда":
                    names = new String[]{"Женская", "Мужская", "Детская", "Обувь", "Аксессуары"};
                    break;
                default:
                    names = new String[]{"Прочее"};
            }

            List<SubcategoryResponse> created = new ArrayList<>();
            for (String subName : names) {
                SubcategoryCreateRequest req = new SubcategoryCreateRequest();
                req.setName(subName);
                req.setDescription("Подкатегория " + subName);
                req.setCategoryId(cat.getId());
                try {
                    SubcategoryResponse resp = retryFeignCall(() -> subcategoryServiceClient.createSubcategory(req));
                    created.add(resp);
                } catch (Exception e) {
                    System.err.println("Ошибка создания подкатегории " + subName + ": " + e.getMessage());
                }
            }
            result.put(cat.getId(), created);
        }
        return result;
    }

    // Generic retry wrapper for Feign calls with service unavailability handling
    private <T> T retryFeignCall(CallableWithResult<T> callable) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return callable.call();
            } catch (FeignException e) {
                lastException = e;
                int status = e.status() < 0 ? 0 : e.status(); // negative means connection error
                
                // Retry on 5xx errors, 408 (timeout), 429 (rate limit), or connection errors (status < 0)
                if (e.status() >= 500 || e.status() == 408 || e.status() == 429 || e.status() < 0) {
                    logRetryAttempt("feign error", status, attempt);
                } else {
                    // 4xx errors (except 408, 429) - don't retry
                    throw e;
                }
            } catch (Exception e) {
                // Other exceptions - retry (might be service not registered yet)
                lastException = e;
                logRetryAttempt("general error", 0, attempt);
            }
            
            if (attempt < MAX_RETRIES) {
                long delay = RETRY_DELAY_MS * attempt; // linear backoff
                Thread.sleep(delay);
            }
        }
        throw new Exception("Не удалось выполнить запрос после " + MAX_RETRIES + " попыток", lastException);
    }

    private void logRetryAttempt(String errorType, int statusCode, int attempt) {
        System.err.printf("Попытка %d/%d: ошибка %s (status=%d). Повтор через %d мс...%n",
            attempt, MAX_RETRIES, errorType, statusCode, RETRY_DELAY_MS * attempt);
    }

    @FunctionalInterface
    private interface CallableWithResult<T> {
        T call() throws Exception;
    }

    private void generateChatMessages(List<UserResponse> users) {
        if (users.size() < 2) {
            System.out.println("Недостаточно пользователей для генерации чатов (нужно минимум 2).");
            return;
        }
        int messageCount = Math.max(users.size() * 2, 10); // at least 10 messages
        for (int i = 0; i < messageCount; i++) {
            UserResponse sender = users.get(random.nextInt(users.size()));
            UserResponse receiver;
            do {
                receiver = users.get(random.nextInt(users.size()));
            } while (receiver.getId().equals(sender.getId()));

            ChatMessageRequest req = new ChatMessageRequest();
            req.setReceiverKeycloakId(receiver.getKeycloakId());
            req.setMessage(faker.lorem().sentence());

            String token = generateDummyToken(sender.getKeycloakId());
            try {
                chatServiceClient.sendMessage("Bearer " + token, req);
            } catch (Exception e) {
                System.err.println("Ошибка при отправке сообщения в чат: " + e.getMessage());
            }
        }
    }

    private String generateDummyToken(String keycloakId) {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        return Jwts.builder()
                .setSubject(keycloakId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }

    @Transactional
    public void clearAllPosts() {
        postRepository.deleteAll();
    }
}
