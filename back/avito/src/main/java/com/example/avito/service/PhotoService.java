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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final PostRepository postRepository;
    private final PhotoMapper photoMapper;

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

    public List<PhotoResponse> getPhotosByPost(Long postId) {
        return photoMapper.toResponseList(photoRepository.findByPostId(postId));
    }

    public PhotoResponse getPhotoById(Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Фото не найдено"));
        return photoMapper.toResponse(photo);
    }

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
}