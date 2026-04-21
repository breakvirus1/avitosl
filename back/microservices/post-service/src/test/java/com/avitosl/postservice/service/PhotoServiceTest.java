package com.avitosl.postservice.service;

import com.avitosl.postservice.entity.Photo;
import com.avitosl.postservice.entity.Post;
import com.avitosl.postservice.exception.NotFoundException;
import com.avitosl.postservice.mapper.PhotoMapper;
import com.avitosl.postservice.repository.PhotoRepository;
import com.avitosl.postservice.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private PhotoMapper photoMapper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PhotoService photoService;

    private Post post;
    private Photo photo1;
    private Photo photo2;

    @BeforeEach
    void setUp() throws IOException {
        post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");
        post.setKeycloakId("user-123");

        photo1 = new Photo();
        photo1.setId(1L);
        photo1.setFileName("photo1.jpg");
        photo1.setFilePath("/uploads/photo1.jpg");
        photo1.setFileSize(1024L);
        photo1.setContentType("image/jpeg");
        photo1.setIsPrimary(true);
        photo1.setPost(post);
        photo1.setCreatedAt(LocalDateTime.now());

        photo2 = new Photo();
        photo2.setId(2L);
        photo2.setFileName("photo2.jpg");
        photo2.setFilePath("/uploads/photo2.jpg");
        photo2.setFileSize(2048L);
        photo2.setContentType("image/jpeg");
        photo2.setIsPrimary(false);
        photo2.setPost(post);
        photo2.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createPhoto_ShouldSavePhoto() {
        // Given
        when(photoRepository.save(any(Photo.class))).thenAnswer(invocation -> {
            Photo p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        Photo photo = new Photo();
        photo.setFileName("test.jpg");
        photo.setPost(post);

        // When
        Photo result = photoService.createPhoto(photo);

        // Then
        assertNotNull(result);
        verify(photoRepository).save(any(Photo.class));
    }

    @Test
    void uploadPhoto_ShouldStoreFileAndSavePhotoRecord() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("image/jpeg");

        when(fileStorageService.storeFile(file)).thenReturn("/uploads/uuid_test.jpg");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(photoRepository.save(any(Photo.class))).thenAnswer(invocation -> {
            Photo p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        // When
        Photo result = photoService.uploadPhoto(file, 1L, true);

        // Then
        assertNotNull(result);
        assertEquals("/uploads/uuid_test.jpg", result.getFilePath());
        assertEquals("test.jpg", result.getFileName());
        assertTrue(result.getIsPrimary());
        verify(fileStorageService).storeFile(file);
        verify(postRepository).findById(1L);
        verify(photoRepository).save(any(Photo.class));
    }

    @Test
    void uploadPhoto_ShouldThrowNotFoundException_WhenPostDoesNotExist() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> {
            photoService.uploadPhoto(file, 999L, false);
        });
        verify(fileStorageService).storeFile(file);
        verify(photoRepository, never()).save(any(Photo.class));
    }

    @Test
    void getPhotoById_ShouldReturnPhoto_WhenExists() {
        // Given
        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo1));

        // When
        Photo result = photoService.getPhotoById(1L);

        // Then
        assertNotNull(result);
        assertEquals("photo1.jpg", result.getFileName());
        verify(photoRepository).findById(1L);
    }

    @Test
    void getPhotoById_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(photoRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> photoService.getPhotoById(999L));
        verify(photoRepository).findById(999L);
    }

    @Test
    void getPhotosByPostId_ShouldReturnPhotos() {
        // Given
        when(photoRepository.findByPostId(1L)).thenReturn(Arrays.asList(photo1, photo2));

        // When
        List<Photo> result = photoService.getPhotosByPostId(1L);

        // Then
        assertEquals(2, result.size());
        verify(photoRepository).findByPostId(1L);
    }

    @Test
    void updatePhoto_ShouldUpdateFields_WhenExists() {
        // Given
        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo1));
        when(photoRepository.save(any(Photo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Photo updateDetails = new Photo();
        updateDetails.setFilePath("/updated/path.jpg");
        updateDetails.setFileName("updated.jpg");
        updateDetails.setFileSize(1111L);
        updateDetails.setContentType("image/jpeg");
        updateDetails.setIsPrimary(false);

        // When
        Photo result = photoService.updatePhoto(1L, updateDetails);

        // Then
        assertEquals("/updated/path.jpg", result.getFilePath());
        assertEquals("updated.jpg", result.getFileName());
        assertFalse(result.getIsPrimary());
        verify(photoRepository).findById(1L);
        verify(photoRepository).save(any(Photo.class));
    }

    @Test
    void updatePhoto_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(photoRepository.findById(999L)).thenReturn(Optional.empty());

        Photo updateDetails = new Photo();
        updateDetails.setFileName("new.jpg");

        // When/Then
        assertThrows(NotFoundException.class, () -> photoService.updatePhoto(999L, updateDetails));
        verify(photoRepository).findById(999L);
        verify(photoRepository, never()).save(any(Photo.class));
    }

    @Test
    void deletePhoto_ShouldDeleteFileAndPhotoRecord_WhenExists() throws IOException {
        // Given
        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo1));
        doNothing().when(fileStorageService).deleteFile(anyString());
        doNothing().when(photoRepository).delete(any(Photo.class));

        // When
        photoService.deletePhoto(1L);

        // Then
        verify(photoRepository).findById(1L);
        verify(fileStorageService).deleteFile(photo1.getFilePath());
        verify(photoRepository).delete(photo1);
    }

    @Test
    void setPrimaryPhoto_ShouldSetOnePrimaryAndOthersNot() {
        // Given
        when(photoRepository.findByPostId(1L)).thenReturn(Arrays.asList(photo1, photo2));
        when(photoRepository.save(any(Photo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        photoService.setPrimaryPhoto(1L, 2L); // set photo2 as primary

        // Then
        assertFalse(photo1.getIsPrimary());
        assertTrue(photo2.getIsPrimary());
        verify(photoRepository, times(2)).save(any(Photo.class));
    }

    @Test
    void getPhotoFile_ShouldReturnBytes_WhenFileExists() throws IOException {
        // Given
        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo1));
        when(fileStorageService.loadFile("/uploads/photo1.jpg")).thenReturn(new byte[]{1,2,3});

        // When
        byte[] result = photoService.getPhotoFile(1L);

        // Then
        assertArrayEquals(new byte[]{1,2,3}, result);
        verify(photoRepository).findById(1L);
        verify(fileStorageService).loadFile("/uploads/photo1.jpg");
    }

    @Test
    void getPhotoFile_ShouldThrowNotFoundException_WhenPhotoNotExists() throws IOException {
        // Given
        when(photoRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> photoService.getPhotoFile(999L));
        verify(photoRepository).findById(999L);
        verify(fileStorageService, never()).loadFile(anyString());
    }
}
