package com.avitosl.postservice.service;

import com.avitosl.postservice.entity.Photo;
import com.avitosl.postservice.exception.NotFoundException;
import com.avitosl.postservice.mapper.PhotoMapper;
import com.avitosl.postservice.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final PhotoMapper photoMapper;

    public Photo createPhoto(Photo photo) {
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
        photoRepository.delete(photo);
    }

    public void setPrimaryPhoto(Long postId, Long photoId) {
        // Remove primary flag from all photos for this post
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
}
