package com.example.avito.service;

import com.example.avito.entity.Image;
import com.example.avito.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;

    public Image createImage(Image image) {
        return imageRepository.save(image);
    }

    public List<Image> findByPostId(Long postId) {
        return imageRepository.findByPostId(postId);
    }

    public void deleteImage(Long id) {
        imageRepository.deleteById(id);
    }
}