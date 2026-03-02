package com.example.avito.service;

import com.example.avito.dto.response.PostResponse;
import com.example.avito.entity.Post;
import com.example.avito.mapper.PostMapper;
import com.example.avito.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {
    
    private PostRepository postRepository;

    private PostMapper postMapper;

    public PostResponse createPost(Post post) {
        Post savedPost = postRepository.save(post);
        return postMapper.toResponse(savedPost);
    }

    public List<PostResponse> findAllPosts() {
        List<Post> posts = postRepository.findAll();
        return posts.stream()
                .map(postMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<PostResponse> findByCategory(String category) {
        List<Post> posts = postRepository.findByCategory(category);
        return posts.stream()
                .map(postMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PostResponse updatePost(Long id, Post post) {
        Optional<Post> existingPostOptional = postRepository.findById(id);
        if (existingPostOptional.isPresent()) {
            Post existingPost = existingPostOptional.get();
            existingPost.setTitle(post.getTitle());
            existingPost.setDescription(post.getDescription());
            existingPost.setPrice(post.getPrice());
            existingPost.setCategory(post.getCategory());
            
            Post updatedPost = postRepository.save(existingPost);
            return postMapper.toResponse(updatedPost);
        }
        return null;
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}