package com.avitosl.postservice.repository;

import com.avitosl.postservice.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);
    List<Post> findByCategoryId(Long categoryId);
    List<Post> findBySubcategoryId(Long subcategoryId);
    List<Post> findByIsActiveTrue();
    List<Post> findByTitleContainingIgnoreCase(String title);
}
