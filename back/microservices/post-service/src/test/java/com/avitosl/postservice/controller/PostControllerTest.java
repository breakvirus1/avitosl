package com.avitosl.postservice.controller;

import com.avitosl.postservice.entity.Post;
import com.avitosl.postservice.feign.UserServiceClient;
import com.avitosl.postservice.request.PostRequest;
import com.avitosl.postservice.request.PostUpdateRequest;
import com.avitosl.postservice.response.PostResponse;
import com.avitosl.postservice.response.UserResponse;
import com.avitosl.postservice.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private UserServiceClient userServiceClient;

    @Test
    void createPost_ShouldReturnPostResponse() throws Exception {
        // Given
        PostRequest request = new PostRequest();
        request.setTitle("Test Post");
        request.setDescription("Desc");
        request.setPrice(100.0);
        request.setKeycloakId("user-123");
        request.setCategoryId(1L);
        request.setSubcategoryId(1L);
        request.setIsActive(true);

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");
        post.setKeycloakId("user-123");
        post.setPrice(100.0);

        PostResponse response = new PostResponse();
        response.setId(1L);
        response.setTitle("Test Post");

        when(postService.createPost(any(Post.class))).thenReturn(post);
        // The controller maps to response manually; we just check JSON mapping.

        // When/Then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Post"));
    }

    @Test
    void getPostById_ShouldReturnPost() throws Exception {
        // Given
        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test");
        when(postService.getPostById(1L)).thenReturn(post);

        // When/Then
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    void getPostsByKeycloakId_ShouldReturnList() throws Exception {
        // Given
        Post post1 = new Post();
        post1.setTitle("Post1");
        Post post2 = new Post();
        post2.setTitle("Post2");
        when(postService.getPostsByKeycloakId("user-123")).thenReturn(Arrays.asList(post1, post2));

        // When/Then
        mockMvc.perform(get("/api/posts/user/user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Post1"));
    }

    @Test
    void getActivePosts_ShouldReturnActiveOnly() throws Exception {
        // Given
        Post active = new Post();
        active.setIsActive(true);
        when(postService.getActivePosts()).thenReturn(Arrays.asList(active));

        // When/Then
        mockMvc.perform(get("/api/posts/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    void searchPosts_ShouldReturnMatches() throws Exception {
        // Given
        Post post = new Post();
        post.setTitle("iPhone");
        when(postService.searchPostsByTitle("iphone")).thenReturn(Arrays.asList(post));

        // When/Then
        mockMvc.perform(get("/api/posts/search")
                        .param("title", "iphone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("iPhone"));
    }

    @Test
    void getPostsWithPagination_ShouldReturnPage() throws Exception {
        // Given
        Post post = new Post();
        post.setTitle("Test");
        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        Page<Post> page = new PageImpl<>(Arrays.asList(post), pageable, 1);
        when(postService.getPostsWithPagination(any(Pageable.class))).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/posts")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test"));
    }

    @Test
    void updatePost_ShouldUpdateAndReturn() throws Exception {
        // Given
        PostUpdateRequest request = new PostUpdateRequest();
        request.setTitle("Updated");
        request.setDescription("Updated desc");
        request.setPrice(200.0);

        Post updated = new Post();
        updated.setId(1L);
        updated.setTitle("Updated");
        updated.setDescription("Updated desc");
        updated.setPrice(200.0);

        when(postService.updatePost(eq(1L), any(Post.class))).thenReturn(updated);

        // When/Then
        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void deletePost_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(postService).deletePost(1L);

        // When/Then
        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void activatePost_ShouldReturnOk() throws Exception {
        // Given
        doNothing().when(postService).activatePost(1L);

        // When/Then
        mockMvc.perform(post("/api/posts/1/activate"))
                .andExpect(status().isOk());
    }

    @Test
    void deactivatePost_ShouldReturnOk() throws Exception {
        // Given
        doNothing().when(postService).deactivatePost(1L);

        // When/Then
        mockMvc.perform(post("/api/posts/1/deactivate"))
                .andExpect(status().isOk());
    }
}
