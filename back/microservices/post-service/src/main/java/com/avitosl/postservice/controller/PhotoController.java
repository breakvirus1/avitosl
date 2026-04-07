package com.avitosl.postservice.controller;

import com.avitosl.postservice.entity.Photo;
import com.avitosl.postservice.request.PhotoRequest;
import com.avitosl.postservice.response.PhotoResponse;
import com.avitosl.postservice.service.PhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping
    public ResponseEntity<PhotoResponse> createPhoto(@Valid @RequestBody PhotoRequest request) {
        Photo photo = photoService.createPhoto(
                new Photo(null, request.getFilePath(), request.getFileName(), request.getFileSize(),
                        request.getContentType(), null, request.getIsPrimary(), null)
        );
        return ResponseEntity.ok(mapToResponse(photo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getPhotoById(@PathVariable Long id) {
        Photo photo = photoService.getPhotoById(id);
        return ResponseEntity.ok(mapToResponse(photo));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<PhotoResponse>> getPhotosByPostId(@PathVariable Long postId) {
        List<Photo> photos = photoService.getPhotosByPostId(postId);
        List<PhotoResponse> responses = photos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhotoResponse> updatePhoto(@PathVariable Long id,
                                                     @Valid @RequestBody PhotoRequest request) {
        Photo photoDetails = new Photo(null, request.getFilePath(), request.getFileName(), request.getFileSize(),
                request.getContentType(), null, request.getIsPrimary(), null);
        Photo updatedPhoto = photoService.updatePhoto(id, photoDetails);
        return ResponseEntity.ok(mapToResponse(updatedPhoto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/primary/{photoId}")
    public ResponseEntity<Void> setPrimaryPhoto(@PathVariable Long postId, @PathVariable Long photoId) {
        photoService.setPrimaryPhoto(postId, photoId);
        return ResponseEntity.ok().build();
    }

    private PhotoResponse mapToResponse(Photo photo) {
        return new PhotoResponse(
                photo.getId(),
                photo.getFilePath(),
                photo.getFileName(),
                photo.getFileSize(),
                photo.getContentType(),
                photo.getPost() != null ? photo.getPost().getId() : null,
                photo.getIsPrimary(),
                photo.getCreatedAt()
        );
    }
}
