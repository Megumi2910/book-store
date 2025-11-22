package com.second_project.book_store.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for image upload operations.
 * Handles image validation and storage.
 */
public interface ImageUploadService {

    /**
     * Upload an image file to the static directory.
     * 
     * @param file Image file
     * @param folder Target folder (e.g., "books", "users")
     * @return Relative URL path to the uploaded image
     * @throws IllegalArgumentException if file is invalid
     */
    String uploadImage(MultipartFile file, String folder);

    /**
     * Delete an image file.
     * 
     * @param imageUrl Relative URL of the image
     * @return true if deleted successfully
     */
    boolean deleteImage(String imageUrl);

    /**
     * Validate image file.
     * Checks file size, format, etc.
     * 
     * @param file Image file
     * @throws IllegalArgumentException if validation fails
     */
    void validateImage(MultipartFile file);

    /**
     * Get default placeholder image URL.
     * 
     * @return Placeholder image URL
     */
    String getDefaultPlaceholderUrl();
}

