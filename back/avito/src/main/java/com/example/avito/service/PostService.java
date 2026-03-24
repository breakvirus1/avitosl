package com.example.avito.service;

import com.example.avito.entity.Category;
import com.example.avito.entity.Comment;
import com.example.avito.entity.Photo;
import com.example.avito.entity.Post;
import com.example.avito.entity.Subcategory;
import com.example.avito.entity.User;
import com.example.avito.exception.AccessDeniedException;
import com.example.avito.exception.NotFoundException;
import com.example.avito.mapper.PostMapper;
import com.example.avito.repository.CategoryRepository;
import com.example.avito.repository.CommentRepository;
import com.example.avito.repository.PhotoRepository;
import com.example.avito.repository.PostRepository;
import com.example.avito.repository.SubcategoryRepository;
import com.example.avito.repository.UserRepository;
import com.example.avito.request.PostRequest;
import com.example.avito.response.PostResponse;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final PhotoRepository photoRepository;
    private final PostMapper postMapper;

    public PostResponse createPost(PostRequest request, User author) {
        Post post = Post.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice() != null ? BigDecimal.valueOf(request.getPrice()) : null)
                .author(author)
                .active(true)
                .build();

        // Устанавливаем категорию
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            post.setCategory(category);
        }

        // Устанавливаем подкатегорию
        if (request.getSubcategoryId() != null) {
            Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                    .orElseThrow(() -> new NotFoundException("Подкатегория не найдена"));
            post.setSubcategory(subcategory);
        }

        Post savedPost = postRepository.save(post);

        if (request.getPhotoUrls() != null) {
            request.getPhotoUrls().forEach(url -> {
                Photo photo = Photo.builder()
                        .url(url)
                        .primary(false)
                        .post(savedPost)
                        .build();
                photoRepository.save(photo);
            });
        }

        return postMapper.toResponse(savedPost);
    }

    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findByActiveTrue(pageable).map(postMapper::toResponse);
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Объявление не найдено"));
        return postMapper.toResponse(post);
    }

    public PostResponse updatePost(Long id, PostRequest request, User author) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Объявление не найдено"));

        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new AccessDeniedException("Вы не можете редактировать это объявление");
        }

        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setPrice(request.getPrice() != null ? BigDecimal.valueOf(request.getPrice()) : null);

        // Обновляем категорию
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            post.setCategory(category);
        }

        // Обновляем подкатегорию
        if (request.getSubcategoryId() != null) {
            Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                    .orElseThrow(() -> new NotFoundException("Подкатегория не найдена"));
            post.setSubcategory(subcategory);
        }

        post = postRepository.save(post);
        return postMapper.toResponse(post);
    }

    public void deletePost(Long id, User author) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Объявление не найдено"));

        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new AccessDeniedException("Вы не можете удалить это объявление");
        }

        postRepository.delete(post);
    }

    public Page<PostResponse> searchPosts(String title, BigDecimal minPrice, BigDecimal maxPrice, Long subcategoryId, Pageable pageable) {
        return postRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            predicates.add(cb.isTrue(root.get("active")));

            if (title != null && !title.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (subcategoryId != null) {
                Join<Post, Subcategory> join = root.join("subcategory");
                predicates.add(cb.equal(join.get("id"), subcategoryId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(postMapper::toResponse);
    }

    public Page<PostResponse> getPostsByUser(Long userId, Pageable pageable) {
        return postRepository.findByAuthorId(userId, pageable).map(postMapper::toResponse);
    }
}
