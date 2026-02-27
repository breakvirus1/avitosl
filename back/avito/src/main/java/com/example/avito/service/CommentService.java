package com.example.avito.service;

import com.example.avito.entity.Comment;
import com.example.avito.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> findByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}