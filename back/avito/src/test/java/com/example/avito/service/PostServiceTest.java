package com.example.avito.service;

import com.example.avito.entity.Category;
import com.example.avito.entity.Post;
import com.example.avito.entity.Subcategory;
import com.example.avito.entity.User;
import com.example.avito.exception.AccessDeniedException;
import com.example.avito.exception.NotFoundException;
import com.example.avito.mapper.CategoryMapper;
import com.example.avito.mapper.PostMapper;
import com.example.avito.repository.CategoryRepository;
import com.example.avito.repository.PostRepository;
import com.example.avito.repository.SubcategoryRepository;
import com.example.avito.repository.UserRepository;
import com.example.avito.request.PostRequest;
import com.example.avito.response.CategoryResponse;
import com.example.avito.response.PostResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubcategoryRepository subcategoryRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private PostService postService;

    @Test
    void createPost_shouldCreatePostSuccessfully() {
        // Given
        PostRequest request = PostRequest.builder()
                .title("Test Post")
                .description("Test Description")
                .price(100.0)
                .categoryId(1L)
                .subcategoryId(2L)
                .build();

        User author = User.builder().id(1L).build();
        Category category = Category.builder().id(1L).build();
        Subcategory subcategory = Subcategory.builder().id(2L).build();
        Post post = Post.builder().title("Test Post").build();
        Post savedPost = Post.builder().id(1L).title("Test Post").build();
        PostResponse response = PostResponse.builder().id(1L).title("Test Post").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.findById(2L)).thenReturn(Optional.of(subcategory));
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        when(postMapper.toResponse(savedPost)).thenReturn(response);

        // When
        PostResponse result = postService.createPost(request, author);

        // Then
        assertThat(result).isEqualTo(response);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_shouldThrowNotFoundExceptionWhenCategoryNotFound() {
        // Given
        PostRequest request = PostRequest.builder().categoryId(1L).build();
        User author = User.builder().id(1L).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.createPost(request, author))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Категория не найдена");
    }

    @Test
    void createPost_shouldThrowNotFoundExceptionWhenSubcategoryNotFound() {
        // Given
        PostRequest request = PostRequest.builder().subcategoryId(2L).build();
        User author = User.builder().id(1L).build();

        when(subcategoryRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.createPost(request, author))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Подкатегория не найдена");
    }

    @Test
    void getPostById_shouldReturnPost() {
        // Given
        Post post = Post.builder().id(1L).title("Test").build();
        PostResponse response = PostResponse.builder().id(1L).title("Test").build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postMapper.toResponse(post)).thenReturn(response);

        // When
        PostResponse result = postService.getPostById(1L);

        // Then
        assertThat(result).isEqualTo(response);
    }

    @Test
    void getPostById_shouldThrowNotFoundExceptionWhenNotFound() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.getPostById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Объявление не найдено");
    }

    @Test
    void getAllPosts_shouldReturnPosts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = List.of(new Post(), new Post());
        Page<Post> postPage = new PageImpl<>(posts, pageable, 2);
        List<PostResponse> responses = List.of(new PostResponse(), new PostResponse());

        when(postRepository.findByActiveTrue(pageable)).thenReturn(postPage);
        lenient().when(categoryMapper.toResponse(any(Category.class))).thenReturn(new CategoryResponse());
        doReturn(new PostResponse()).when(postMapper).toResponse(any(Post.class));

        // When
        Page<PostResponse> result = postService.getAllPosts(pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void updatePost_shouldUpdatePostSuccessfully() {
        // Given
        PostRequest request = PostRequest.builder()
                .title("Updated Post")
                .description("Updated Description")
                .price(200.0)
                .categoryId(1L)
                .subcategoryId(2L)
                .build();

        User author = User.builder().id(1L).build();
        Post existingPost = Post.builder().id(1L).author(author).build();
        Category category = Category.builder().id(1L).build();
        Subcategory subcategory = Subcategory.builder().id(2L).build();
        PostResponse response = PostResponse.builder().id(1L).title("Updated Post").build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.findById(2L)).thenReturn(Optional.of(subcategory));
        when(postRepository.save(any(Post.class))).thenReturn(existingPost);
        when(postMapper.toResponse(any(Post.class))).thenReturn(response);

        // When
        PostResponse result = postService.updatePost(1L, request, author);

        // Then
        assertThat(result).isEqualTo(response);
        verify(postRepository).save(existingPost);
    }

    @Test
    void updatePost_shouldThrowAccessDeniedExceptionWhenNotAuthor() {
        // Given
        PostRequest request = PostRequest.builder().build();
        User author = User.builder().id(1L).build();
        User differentAuthor = User.builder().id(2L).build();
        Post post = Post.builder().id(1L).author(differentAuthor).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // When & Then
        assertThatThrownBy(() -> postService.updatePost(1L, request, author))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Вы не можете редактировать это объявление");
    }

    @Test
    void deletePost_shouldDeletePost() {
        // Given
        User author = User.builder().id(1L).build();
        Post post = Post.builder().id(1L).author(author).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // When
        postService.deletePost(1L, author);

        // Then
        verify(postRepository).delete(post);
    }

    @Test
    void deletePost_shouldThrowAccessDeniedExceptionWhenNotAuthor() {
        // Given
        User author = User.builder().id(1L).build();
        User differentAuthor = User.builder().id(2L).build();
        Post post = Post.builder().id(1L).author(differentAuthor).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // When & Then
        assertThatThrownBy(() -> postService.deletePost(1L, author))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Вы не можете удалить это объявление");
    }
}