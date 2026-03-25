package com.example.avito.repository;

import com.example.avito.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByPostId(Long postId);
    List<Photo> findByPostIdAndPrimaryTrue(Long postId);
}