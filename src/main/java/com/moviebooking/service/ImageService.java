package com.moviebooking.service;

import com.moviebooking.exception.CustomExceptions.ImageUploadException;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String uploadImage(MultipartFile file, String folder) throws ImageUploadException;

    void deleteImage(String publicId) throws Exception;

}