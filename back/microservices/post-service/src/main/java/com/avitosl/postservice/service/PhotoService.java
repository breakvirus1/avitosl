package com.avitosl.postservice.service;

import com.avitosl.postservice.entity.Photo;
import com.avitosl.postservice.entity.Post;
import com.avitosl.postservice.exception.NotFoundException;
import com.avitosl.postservice.mapper.PhotoMapper;
import com.avitosl.postservice.repository.PhotoRepository;
import com.avitosl.postservice.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final PhotoMapper photoMapper;
    private final FileStorageService fileStorageService;
    private final PostRepository postRepository;

    public Photo createPhoto(Photo photo) {
        return photoRepository.save(photo);
    }

    public Photo uploadPhoto(MultipartFile file, Long postId, Boolean isPrimary) throws IOException {
        String storedFilePath = fileStorageService.storeFile(file);
        String fileName = file.getOriginalFilename();
        Long fileSize = file.getSize();
        String contentType = file.getContentType();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));

        Photo photo = new Photo();
        photo.setFilePath(storedFilePath);
        photo.setFileName(fileName);
        photo.setFileSize(fileSize);
        photo.setContentType(contentType);
        photo.setIsPrimary(isPrimary != null ? isPrimary : false);
        photo.setPost(post);

        return photoRepository.save(photo);
    }

    public Photo getPhotoById(Long id) {
        return photoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Photo not found with id: " + id));
    }

    public List<Photo> getPhotosByPostId(Long postId) {
        return photoRepository.findByPostId(postId);
    }

    public Photo updatePhoto(Long id, Photo photoDetails) {
        Photo photo = getPhotoById(id);

        photoMapper.updateEntityFromRequest(null, photo);
        photo.setFilePath(photoDetails.getFilePath());
        photo.setFileName(photoDetails.getFileName());
        photo.setFileSize(photoDetails.getFileSize());
        photo.setContentType(photoDetails.getContentType());
        photo.setIsPrimary(photoDetails.getIsPrimary());

        return photoRepository.save(photo);
    }

    public void deletePhoto(Long id) {
        Photo photo = getPhotoById(id);
        fileStorageService.deleteFile(photo.getFilePath());
        photoRepository.delete(photo);
    }

    public void setPrimaryPhoto(Long postId, Long photoId) {
        List<Photo> existingPhotos = photoRepository.findByPostId(postId);
        existingPhotos.forEach(photo -> {
            if (photo.getId().equals(photoId)) {
                photo.setIsPrimary(true);
            } else {
                photo.setIsPrimary(false);
            }
            photoRepository.save(photo);
        });
    }

    public byte[] getPhotoFile(Long id) throws IOException {
        Photo photo = getPhotoById(id);
        return fileStorageService.loadFile(photo.getFilePath());
    }
}
