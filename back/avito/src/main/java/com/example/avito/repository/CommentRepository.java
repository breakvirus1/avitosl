package com.example.avito.repository;

import com.example.avito.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT DISTINCT c FROM Comment c JOIN FETCH c.author JOIN FETCH c.post WHERE c.post.id = :postId ORDER BY c.createdAt DESC")
    List<Comment> findByPostId(@Param("postId") Long postId);
    
    @Query("SELECT DISTINCT c FROM Comment c JOIN FETCH c.author JOIN FETCH c.post WHERE c.author.id = :authorId ORDER BY c.createdAt DESC")
    List<Comment> findByAuthorId(@Param("authorId") Long authorId);
}