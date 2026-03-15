package com.example.avito.repository;

import com.example.avito.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    Page<Post> findByActiveTrue(Pageable pageable);
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);
    List<Post> findByAuthorId(Long authorId);
    List<Post> findByActiveTrue();
    List<Post> findBySubcategory_Id(Long subcategoryId);
}