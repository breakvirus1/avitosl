package com.avitosl.postservice.mapper;

import com.avitosl.postservice.entity.Post;
import com.avitosl.postservice.request.PostRequest;
import com.avitosl.postservice.response.PostResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostMapperImplTest {

    private PostMapper postMapper;

    @BeforeEach
    void setUp() {
        postMapper = Mappers.getMapper(PostMapper.class);
    }

    @Test
    void toEntity_ShouldConvertPostRequestToPost() {
        // Given
        PostRequest request = new PostRequest();
        request.setTitle("Test Post");
        request.setDescription("Description");
        request.setPrice(99.99);
        request.setKeycloakId("user-1");
        request.setCategoryId(1L);
        request.setSubcategoryId(2L);
        request.setIsActive(true);

        // When
        Post entity = postMapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getTitle()).isEqualTo("Test Post");
        assertThat(entity.getDescription()).isEqualTo("Description");
        assertThat(entity.getPrice()).isEqualTo(99.99);
        assertThat(entity.getKeycloakId()).isEqualTo("user-1");
        assertThat(entity.getCategoryId()).isEqualTo(1L);
        assertThat(entity.getSubcategoryId()).isEqualTo(2L);
        assertThat(entity.getIsActive()).isTrue();
    }

    @Test
    void toResponse_ShouldConvertPostToResponse() {
        // Given
        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");
        post.setDescription("Description");
        post.setPrice(99.99);
        post.setKeycloakId("user-1");
        post.setCategoryId(1L);
        post.setSubcategoryId(2L);
        post.setIsActive(true);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // When
        PostResponse response = postMapper.toResponse(post);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Post");
        assertThat(response.getDescription()).isEqualTo("Description");
        assertThat(response.getPrice()).isEqualTo(99.99);
        assertThat(response.getKeycloakId()).isEqualTo("user-1");
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getSubcategoryId()).isEqualTo(2L);
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    void updateEntityFromRequest_ShouldUpdateEntityFields() {
        // Given
        Post post = new Post();
        post.setId(1L);
        post.setTitle("Old Title");
        post.setDescription("Old description");
        post.setPrice(50.0);
        post.setKeycloakId("user-1");
        post.setCategoryId(1L);
        post.setSubcategoryId(2L);
        post.setIsActive(true); // initial value

        PostRequest request = new PostRequest();
        request.setTitle("New Title");
        request.setDescription("New description");
        request.setPrice(100.0);
        request.setKeycloakId("user-2");
        request.setCategoryId(3L);
        request.setSubcategoryId(4L);
        request.setIsActive(false); // this will be ignored by mapper

        // When
        postMapper.updateEntityFromRequest(request, post);

        // Then - check updated fields
        assertThat(post.getTitle()).isEqualTo("New Title");
        assertThat(post.getDescription()).isEqualTo("New description");
        assertThat(post.getPrice()).isEqualTo(100.0);
        assertThat(post.getKeycloakId()).isEqualTo("user-2");
        assertThat(post.getCategoryId()).isEqualTo(3L);
        assertThat(post.getSubcategoryId()).isEqualTo(4L);
        // isActive is ignored by mapper (by design)
        assertThat(post.getIsActive()).isTrue();
    }
}
