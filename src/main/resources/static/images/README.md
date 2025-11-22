# Images Directory

This directory contains images for the Book Store application.

## Directory Structure

```
images/
├── books/          # Book cover images (uploaded by admin)
├── users/          # User profile pictures (future)
├── placeholder.jpg # Default placeholder image
└── README.md       # This file
```

## Image Upload Guidelines

### Book Covers
- **Location**: `/images/books/`
- **Formats**: JPG, JPEG, PNG, WEBP
- **Max Size**: 2MB
- **Recommended Dimensions**: 400x600px (2:3 ratio)

### Placeholder Image
- Used when no book cover is provided
- Path: `/images/placeholder.jpg`

## Notes

- Images are served as static resources by Spring Boot
- Filenames are automatically generated as UUIDs to avoid conflicts
- In production, consider using a CDN or cloud storage (AWS S3, Cloudinary) for better performance and scalability

## Future Enhancements

1. **Image Optimization**: Add automatic image compression/resizing
2. **Cloud Storage**: Migrate to AWS S3 or similar for scalability
3. **CDN Integration**: Use CloudFront or similar for faster delivery
4. **Image Variants**: Generate thumbnails and different sizes automatically

