package com.avitosl.postservice.service;

import com.avitosl.postservice.entity.Post;
import com.avitosl.postservice.exception.ConflictException;
import com.avitosl.postservice.exception.NotFoundException;
import com.avitosl.postservice.feign.CategoryServiceClient;
import com.avitosl.postservice.feign.UserServiceClient;
import com.avitosl.postservice.mapper.PostMapper;
import com.avitosl.postservice.repository.PostRepository;
import com.avitosl.postservice.response.CategoryResponse;
import com.avitosl.postservice.response.UserResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserServiceClient userServiceClient;
    private final CategoryServiceClient categoryServiceClient;

    public Post createPost(Post post) {
        // Проверяем существование пользователя через Feign вызов
        try {
            UserResponse user = userServiceClient.getUserByKeycloakId(post.getKeycloakId());
            if (user == null) {
                throw new NotFoundException("User not found with keycloakId: " + post.getKeycloakId());
            }
        } catch (RuntimeException e) {
            // Обработка fallback и других исключений
            if (e.getMessage().contains("User service is unavailable")) {
                // Логируем, но не прерываем создание поста (опционально)
                // Можно добавить логирование: log.warn("User service unavailable", e);
            } else {
                throw e;
            }
        }
        
        // Проверяем существование категории через Feign вызов
        try {
            CategoryResponse category = categoryServiceClient.getCategoryById(post.getCategoryId());
            if (category == null) {
                throw new NotFoundException("Category not found with id: " + post.getCategoryId());
            }
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Category service is unavailable")) {
                // Логируем, но не прерываем создание поста
            } else {
                throw e;
            }
        }
        
        return postRepository.save(post);
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + id));
    }

    public List<Post> getPostsByKeycloakId(String keycloakId) {
        return postRepository.findByKeycloakId(keycloakId);
    }

    public List<Post> getPostsByCategoryId(Long categoryId) {
        return postRepository.findByCategoryId(categoryId);
    }

    public List<Post> getActivePosts() {
        return postRepository.findByIsActiveTrue();
    }

    public List<Post> searchPostsByTitle(String title) {
        return postRepository.findByTitleContainingIgnoreCase(title);
    }

    public Post updatePost(Long id, Post postDetails) {
        Post post = getPostById(id);

        if (postDetails.getTitle() != null) {
            post.setTitle(postDetails.getTitle());
        }
        if (postDetails.getDescription() != null) {
            post.setDescription(postDetails.getDescription());
        }
        if (postDetails.getPrice() != null) {
            post.setPrice(postDetails.getPrice());
        }
        if (postDetails.getCategoryId() != null) {
            post.setCategoryId(postDetails.getCategoryId());
        }
        if (postDetails.getSubcategoryId() != null) {
            post.setSubcategoryId(postDetails.getSubcategoryId());
        }
        if (postDetails.getIsActive() != null) {
            post.setIsActive(postDetails.getIsActive());
        }
        // keycloakId не обновляется

        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        Post post = getPostById(id);
        postRepository.delete(post);
    }

    public void activatePost(Long id) {
        Post post = getPostById(id);
        post.setIsActive(true);
        postRepository.save(post);
    }

    public void deactivatePost(Long id) {
        Post post = getPostById(id);
        post.setIsActive(false);
        postRepository.save(post);
    }

    public Page<Post> getPostsWithPagination(Pageable pageable) {
        return postRepository.findAll(pageable);
    }
}
