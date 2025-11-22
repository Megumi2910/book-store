package com.second_project.book_store.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.second_project.book_store.service.ImageUploadService;

/**
 * Implementation of ImageUploadService.
 * 
 * BEST PRACTICES:
 * - Validate file size and format
 * - Generate unique filenames to avoid collisions
 * - Store in static directory for web serving
 * - Easy to migrate to cloud storage (S3, Cloudinary) later
 */
@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    private static final Logger logger = LoggerFactory.getLogger(ImageUploadServiceImpl.class);

    // Configuration
    private static final String UPLOAD_DIR = "src/main/resources/static/images/";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final String DEFAULT_PLACEHOLDER = "/images/placeholder.jpg";

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        // Validate file
        validateImage(file);

        try {
            // Create directory if not exists
            Path uploadPath = Paths.get(UPLOAD_DIR + folder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath);
            }

            // Generate unique filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + fileExtension;

            // Save file
            Path targetPath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Image uploaded successfully: {}", newFilename);

            // Return relative URL
            return "/images/" + folder + "/" + newFilename;

        } catch (IOException e) {
            logger.error("Failed to upload image", e);
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.equals(DEFAULT_PLACEHOLDER)) {
            return false;
        }

        try {
            // Convert URL to file path
            String filePath = UPLOAD_DIR + imageUrl.replace("/images/", "");
            Path path = Paths.get(filePath);

            if (Files.exists(path)) {
                Files.delete(path);
                logger.info("Image deleted successfully: {}", imageUrl);
                return true;
            }
        } catch (IOException e) {
            logger.error("Failed to delete image: {}", imageUrl, e);
        }

        return false;
    }

    @Override
    public void validateImage(MultipartFile file) {
        // Check if file is empty
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("File size exceeds maximum allowed size of %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        // Check file extension
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(filename).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                String.format("File type not allowed. Allowed types: %s", String.join(", ", ALLOWED_EXTENSIONS))
            );
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        logger.debug("Image validation passed: {}", filename);
    }

    @Override
    public String getDefaultPlaceholderUrl() {
        return DEFAULT_PLACEHOLDER;
    }

    /**
     * Extract file extension from filename.
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}

