package com.example.avito.service;

import com.example.avito.entity.Comment;
import com.example.avito.entity.Post;
import com.example.avito.entity.User;
import com.example.avito.mapper.CommentMapper;
import com.example.avito.repository.CommentRepository;
import com.example.avito.repository.PostRepository;
import com.example.avito.request.CommentRequest;
import com.example.avito.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;

    public CommentResponse createComment(CommentRequest request, User author) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        Comment comment = Comment.builder()
                .text(request.getText())
                .author(author)
                .post(post)
                .build();

        comment = commentRepository.save(comment);
        return commentMapper.toResponse(comment);
    }

    public List<CommentResponse> getCommentsByPost(Long postId) {
        return commentMapper.toResponseList(commentRepository.findByPostId(postId));
    }

    public List<CommentResponse> getCommentsByUser(Long userId) {
        return commentMapper.toResponseList(commentRepository.findByAuthorId(userId));
    }

    public CommentResponse updateComment(Long id, CommentRequest request, User author) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Комментарий не найден"));

        if (!comment.getAuthor().getId().equals(author.getId())) {
            throw new RuntimeException("Вы не можете редактировать этот комментарий");
        }

        comment.setText(request.getText());
        comment = commentRepository.save(comment);
        return commentMapper.toResponse(comment);
    }

    public void deleteComment(Long id, User author) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Комментарий не найден"));

        if (!comment.getAuthor().getId().equals(author.getId())) {
            throw new RuntimeException("Вы не можете удалить этот комментарий");
        }

        commentRepository.delete(comment);
    }

    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        return commentMapper.toResponse(comment);
    }
}