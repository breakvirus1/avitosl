package com.example.avito.service;

import com.example.avito.entity.Post;
import com.example.avito.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> findByCategory(String category) {
        return postRepository.findByCategory(category);
    }


    public Post updatePost(Long id, Post post) {
        Post existingPost = postRepository.findById(id).orElse(null);
        if (existingPost != null) {
            existingPost.setTitle(post.getTitle());
            existingPost.setDescription(post.getDescription());
            existingPost.setPrice(post.getPrice());
            existingPost.setCategory(post.getCategory());

            return postRepository.save(existingPost);
        }
        return null;
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}