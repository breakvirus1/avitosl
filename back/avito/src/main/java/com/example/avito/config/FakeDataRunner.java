package com.example.avito.config;

import com.example.avito.entity.Category;
import com.example.avito.entity.Subcategory;
import com.example.avito.repository.CategoryRepository;
import com.example.avito.repository.SubcategoryRepository;
import com.example.avito.service.FakeDataGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("dev") // Только для профиля dev
@RequiredArgsConstructor
public class FakeDataRunner implements CommandLineRunner {

    private final FakeDataGeneratorService fakeDataGeneratorService;

    @Override
    public void run(String... args) throws Exception {
        // Автоматически генерируем 20 фейковых объявлений при старте в dev режиме
        try {
            fakeDataGeneratorService.generateFakePosts(20);
            System.out.println("✅ Создано 20 фейковых объявлений при старте приложения");
        } catch (Exception e) {
            System.err.println("⚠️ Не удалось создать фейковые данные: " + e.getMessage());
        }
    }
}
