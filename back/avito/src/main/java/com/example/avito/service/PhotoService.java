package com.example.avito.service;

import com.example.avito.entity.Photo;
import com.example.avito.entity.Post;
import com.example.avito.entity.User;
import com.example.avito.exception.AccessDeniedException;
import com.example.avito.exception.NotFoundException;
import com.example.avito.mapper.PhotoMapper;
import com.example.avito.repository.PhotoRepository;
import com.example.avito.repository.PostRepository;
import com.example.avito.request.PhotoRequest;
import com.example.avito.response.PhotoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final PostRepository postRepository;
    private final PhotoMapper photoMapper;

    @CacheEvict(value = {"photo", "photosByPost"}, allEntries = true)
    public PhotoResponse addPhoto(PhotoRequest request) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new NotFoundException("Объявление не найдено"));

        Photo photo = Photo.builder()
                .url(request.getUrl())
                .primary(request.getPrimary() != null ? request.getPrimary() : false)
                .post(post)
                .build();

        photo = photoRepository.save(photo);
        return photoMapper.toResponse(photo);
    }

    @Cacheable(value = "photosByPost", key = "#postId")
    public List<PhotoResponse> getPhotosByPost(Long postId) {
        return photoMapper.toResponseList(photoRepository.findByPostId(postId));
    }

    @Cacheable(value = "photo", key = "#id")
    public PhotoResponse getPhotoById(Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Фото не найдено"));
        return photoMapper.toResponse(photo);
    }

    @CacheEvict(value = {"photo", "photosByPost"}, allEntries = true)
    public PhotoResponse updatePhoto(Long id, PhotoRequest request, User currentUser) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Фото не найдено"));

        // Проверяем, что пользователь является автором поста, к которому принадлежит фото
        Post post = photo.getPost();
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Вы не можете редактировать это фото");
        }

        photo.setUrl(request.getUrl());
        photo.setPrimary(request.getPrimary() != null ? request.getPrimary() : false);

        photo = photoRepository.save(photo);
        return photoMapper.toResponse(photo);
    }

    @CacheEvict(value = {"photo", "photosByPost"}, key = "#id")
    public void deletePhoto(Long id, User currentUser) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Фото не найдено"));

        // Проверяем, что пользователь является автором поста, к которому принадлежит фото
        Post post = photo.getPost();
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Вы не можете удалить это фото");
        }

        photoRepository.delete(photo);
    }

    @CacheEvict(value = {"photo", "photosByPost"}, allEntries = true)
    public PhotoResponse setPrimaryPhoto(Long photoId, User currentUser) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new NotFoundException("Фото не найдено"));

        // Проверяем, что пользователь является автором поста, к которому принадлежит фото
        Post post = photo.getPost();
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Вы не можете изменить эту фотографию");
        }

        Long postId = photo.getPost().getId();
        photoRepository.findByPostIdAndPrimaryTrue(postId).forEach(p -> {
            if (!p.getId().equals(photoId)) {
                p.setPrimary(false);
                photoRepository.save(p);
            }
        });

        photo.setPrimary(true);
        photo = photoRepository.save(photo);
        return photoMapper.toResponse(photo);
    }

    @Value("${file.upload-dir:uploadedimages}")
    private String uploadDir;

    @CacheEvict(value = "photosByPost", key = "#postId")
    public PhotoResponse uploadFile(MultipartFile file, Long postId, User currentUser) {
        try {
            // Проверяем, что пользователь является автором поста
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new NotFoundException("Объявление не найдено"));
            if (!post.getAuthor().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Вы не можете загружать фото для этого объявления");
            }

            // Создаем директорию для загрузки, если она не существует
            String uploadPath = System.getProperty("user.dir") + File.separator + uploadDir;
            File uploadDirFile = new File(uploadPath);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }

            // Генерируем уникальное имя файла
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Сохраняем файл
            Path filePath = Paths.get(uploadPath, uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            // Создаем запись о фото в базе данных
            String fileUrl = "/uploadedimages/" + uniqueFilename;
            
            Photo photo = Photo.builder()
                    .url(fileUrl)
                    .primary(false)
                    .post(post)
                    .createdAt(LocalDateTime.now())
                    .build();

            photo = photoRepository.save(photo);

            return photoMapper.toResponse(photo);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось загрузить файл: " + e.getMessage());
        }
    }
}