package com.example.avito.service;

import com.example.avito.entity.Comment;
import com.example.avito.entity.Post;
import com.example.avito.entity.User;
import com.example.avito.exception.AccessDeniedException;
import com.example.avito.exception.NotFoundException;
import com.example.avito.mapper.CommentMapper;
import com.example.avito.repository.CommentRepository;
import com.example.avito.repository.PostRepository;
import com.example.avito.request.CommentRequest;
import com.example.avito.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;

    @CacheEvict(value = "commentsByPost", key = "#request.postId")
    public CommentResponse createComment(CommentRequest request, User author) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new NotFoundException("Объявление не найдено"));

        Comment comment = Comment.builder()
                .text(request.getText())
                .author(author)
                .post(post)
                .build();

        comment = commentRepository.save(comment);
        return commentMapper.toResponse(comment);
    }

    @Cacheable(value = "commentsByPost", key = "#postId")
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        // Инициализируем lazy-поля author для каждого комментария
        comments.forEach(comment -> {
            if (comment.getAuthor() != null) {
                comment.getAuthor().getId(); // trigger lazy loading
            }
        });
        return commentMapper.toResponseList(comments);
    }

    @Cacheable(value = "commentsByUser", key = "#userId")
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByUser(Long userId) {
        List<Comment> comments = commentRepository.findByAuthorId(userId);
        // Инициализируем lazy-поля post для каждого комментария
        comments.forEach(comment -> {
            if (comment.getPost() != null) {
                comment.getPost().getId(); // trigger lazy loading
            }
        });
        return commentMapper.toResponseList(comments);
    }

    @CacheEvict(value = {"comment", "commentsByPost", "commentsByUser"}, allEntries = true)
    public CommentResponse updateComment(Long id, CommentRequest request, User author) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));

        if (!comment.getAuthor().getId().equals(author.getId())) {
            throw new AccessDeniedException("Вы не можете редактировать этот комментарий");
        }

        comment.setText(request.getText());
        comment = commentRepository.save(comment);
        return commentMapper.toResponse(comment);
    }

    @CacheEvict(value = {"comment", "commentsByPost", "commentsByUser"}, allEntries = true)
    public void deleteComment(Long id, User author) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));

        if (!comment.getAuthor().getId().equals(author.getId())) {
            throw new AccessDeniedException("Вы не можете удалить этот комментарий");
        }

        commentRepository.delete(comment);
    }

    @Cacheable(value = "comment", key = "#id")
    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        return commentMapper.toResponse(comment);
    }

    @Cacheable(value = "allComments", key = "'all'")
    @Transactional(readOnly = true)
    public List<CommentResponse> getAllComments() {
        List<Comment> comments = commentRepository.findAll();
        // Инициализируем lazy-поля для каждого комментария
        comments.forEach(comment -> {
            if (comment.getAuthor() != null) {
                comment.getAuthor().getId();
            }
            if (comment.getPost() != null) {
                comment.getPost().getId();
            }
        });
        return commentMapper.toResponseList(comments);
    }
}