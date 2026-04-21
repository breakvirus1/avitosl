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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private CategoryServiceClient categoryServiceClient;

    @InjectMocks
    private PostService postService;

    @Captor
    private ArgumentCaptor<String> keycloakIdCaptor;

    private Post post1;
    private Post post2;
    private UserResponse userResponse;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        post1 = new Post();
        post1.setId(1L);
        post1.setTitle("iPhone 15");
        post1.setDescription("New iPhone");
        post1.setPrice(1000.0);
        post1.setKeycloakId("user-123");
        post1.setCategoryId(1L);
        post1.setSubcategoryId(1L);
        post1.setIsActive(true);
        post1.setCreatedAt(LocalDateTime.now());
        post1.setUpdatedAt(LocalDateTime.now());

        post2 = new Post();
        post2.setId(2L);
        post2.setTitle("Samsung Galaxy");
        post2.setDescription("Android phone");
        post2.setPrice(800.0);
        post2.setKeycloakId("user-456");
        post2.setCategoryId(1L);
        post2.setSubcategoryId(2L);
        post2.setIsActive(false);
        post2.setCreatedAt(LocalDateTime.now());
        post2.setUpdatedAt(LocalDateTime.now());

        userResponse = new UserResponse();
        userResponse.setId(123L);
        userResponse.setUsername("testuser");
        userResponse.setKeycloakId("user-123");

        categoryResponse = new CategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Electronics");
    }

    @Test
    void createPost_ShouldSavePost_WhenUserAndCategoryExist() {
        // Given
        when(userServiceClient.getUserByKeycloakId("user-123")).thenReturn(userResponse);
        when(categoryServiceClient.getCategoryById(1L)).thenReturn(categoryResponse);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(1L);
            return post;
        });

        Post newPost = new Post();
        newPost.setTitle("iPhone 15");
        newPost.setDescription("New iPhone");
        newPost.setPrice(1000.0);
        newPost.setKeycloakId("user-123");
        newPost.setCategoryId(1L);
        newPost.setSubcategoryId(1L);

        // When
        Post result = postService.createPost(newPost);

        // Then
        assertNotNull(result);
        assertEquals("iPhone 15", result.getTitle());
        verify(userServiceClient).getUserByKeycloakId("user-123");
        verify(categoryServiceClient).getCategoryById(1L);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        // Given
        when(userServiceClient.getUserByKeycloakId("nonexistent")).thenThrow(new NotFoundException("User not found"));

        Post post = new Post();
        post.setKeycloakId("nonexistent");
        post.setCategoryId(1L);

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            postService.createPost(post);
        });

        assertThat(exception.getMessage()).contains("User not found");
        verify(userServiceClient).getUserByKeycloakId("nonexistent");
        verify(categoryServiceClient, never()).getCategoryById(any());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void createPost_ShouldThrowNotFoundException_WhenCategoryDoesNotExist() {
        // Given
        when(userServiceClient.getUserByKeycloakId("user-123")).thenReturn(userResponse);
        when(categoryServiceClient.getCategoryById(999L)).thenThrow(new NotFoundException("Category not found"));

        Post post = new Post();
        post.setKeycloakId("user-123");
        post.setCategoryId(999L);

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            postService.createPost(post);
        });

        assertThat(exception.getMessage()).contains("Category not found");
        verify(userServiceClient).getUserByKeycloakId("user-123");
        verify(categoryServiceClient).getCategoryById(999L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void createPost_ShouldContinueWhenUserServiceUnavailable_AndSavePost() {
        // Given - fallback returns empty user?
        // Actually PostService catches RuntimeException with message "User service is unavailable" and continues.
        when(userServiceClient.getUserByKeycloakId("user-123")).thenThrow(new RuntimeException("User service is unavailable"));
        when(categoryServiceClient.getCategoryById(1L)).thenReturn(categoryResponse);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(1L);
            return post;
        });

        Post post = new Post();
        post.setTitle("Test Post");
        post.setPrice(100.0);
        post.setKeycloakId("user-123");
        post.setCategoryId(1L);

        // When
        Post result = postService.createPost(post);

        // Then
        assertNotNull(result);
        // Verify that post was saved despite user service failure
        verify(userServiceClient).getUserByKeycloakId("user-123");
        verify(categoryServiceClient).getCategoryById(1L);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_ShouldContinueWhenCategoryServiceUnavailable_AndSavePost() {
        // Given
        when(userServiceClient.getUserByKeycloakId("user-123")).thenReturn(userResponse);
        when(categoryServiceClient.getCategoryById(1L)).thenThrow(new RuntimeException("Category service is unavailable"));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(1L);
            return post;
        });

        Post post = new Post();
        post.setTitle("Test Post");
        post.setPrice(100.0);
        post.setKeycloakId("user-123");
        post.setCategoryId(1L);

        // When
        Post result = postService.createPost(post);

        // Then
        assertNotNull(result);
        verify(userServiceClient).getUserByKeycloakId("user-123");
        verify(categoryServiceClient).getCategoryById(1L);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void getPostById_ShouldReturnPost_WhenExists() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));

        // When
        Post result = postService.getPostById(1L);

        // Then
        assertNotNull(result);
        assertEquals("iPhone 15", result.getTitle());
        verify(postRepository).findById(1L);
    }

    @Test
    void getPostById_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> {
            postService.getPostById(999L);
        });
        verify(postRepository).findById(999L);
    }

    @Test
    void getPostsByKeycloakId_ShouldReturnPosts() {
        // Given
        when(postRepository.findByKeycloakId("user-123")).thenReturn(Arrays.asList(post1));

        // When
        List<Post> result = postService.getPostsByKeycloakId("user-123");

        // Then
        assertEquals(1, result.size());
        verify(postRepository).findByKeycloakId("user-123");
    }

    @Test
    void getPostsByCategoryId_ShouldReturnPosts() {
        // Given
        when(postRepository.findByCategoryId(1L)).thenReturn(Arrays.asList(post1, post2));

        // When
        List<Post> result = postService.getPostsByCategoryId(1L);

        // Then
        assertEquals(2, result.size());
        verify(postRepository).findByCategoryId(1L);
    }

    @Test
    void getActivePosts_ShouldReturnOnlyActivePosts() {
        // Given
        when(postRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(post1));

        // When
        List<Post> result = postService.getActivePosts();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
        verify(postRepository).findByIsActiveTrue();
    }

    @Test
    void searchPostsByTitle_ShouldReturnMatchingPosts() {
        // Given
        when(postRepository.findByTitleContainingIgnoreCase("iphone")).thenReturn(Arrays.asList(post1));

        // When
        List<Post> result = postService.searchPostsByTitle("iphone");

        // Then
        assertEquals(1, result.size());
        verify(postRepository).findByTitleContainingIgnoreCase("iphone");
    }

    @Test
    void updatePost_ShouldUpdateFields_WhenPostExists() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post updateDetails = new Post();
        updateDetails.setTitle("iPhone 15 Pro");
        updateDetails.setDescription("Latest iPhone");
        updateDetails.setPrice(1200.0);

        // When
        Post result = postService.updatePost(1L, updateDetails);

        // Then
        assertNotNull(result);
        assertEquals("iPhone 15 Pro", result.getTitle());
        assertEquals("Latest iPhone", result.getDescription());
        assertEquals(1200.0, result.getPrice());
        // Category ID unchanged
        assertEquals(1L, result.getCategoryId());
        verify(postRepository).findById(1L);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void updatePost_ShouldThrowNotFoundException_WhenPostDoesNotExist() {
        // Given
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        Post updateDetails = new Post();
        updateDetails.setTitle("New Title");

        // When/Then
        assertThrows(NotFoundException.class, () -> {
            postService.updatePost(999L, updateDetails);
        });
        verify(postRepository).findById(999L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deletePost_ShouldDeletePost_WhenExists() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        doNothing().when(postRepository).delete(any(Post.class));

        // When
        postService.deletePost(1L);

        // Then
        verify(postRepository).findById(1L);
        verify(postRepository).delete(post1);
    }

    @Test
    void deletePost_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> {
            postService.deletePost(999L);
        });
        verify(postRepository).findById(999L);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    void activatePost_ShouldSetActiveTrue_WhenExists() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        postService.activatePost(1L);

        // Then
        assertTrue(post1.getIsActive());
        verify(postRepository).findById(1L);
        verify(postRepository).save(post1);
    }

    @Test
    void deactivatePost_ShouldSetActiveFalse_WhenExists() {
        // Given
        post1.setIsActive(true);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        postService.deactivatePost(1L);

        // Then
        assertFalse(post1.getIsActive());
        verify(postRepository).findById(1L);
        verify(postRepository).save(post1);
    }

    @Test
    void getPostsWithPagination_ShouldReturnPage() {
        // Given
        Pageable pageable = Pageable.ofSize(10);
        Page<Post> postPage = new PageImpl<>(Arrays.asList(post1, post2));
        when(postRepository.findAll(pageable)).thenReturn(postPage);

        // When
        Page<Post> result = postService.getPostsWithPagination(pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        verify(postRepository).findAll(pageable);
    }
}
