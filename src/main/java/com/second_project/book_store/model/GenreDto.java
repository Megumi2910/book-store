package com.second_project.book_store.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for Genre entity.
 * Used for admin genre management.
 */
public class GenreDto {

    private Long id;

    @NotBlank(message = "Genre name is required")
    @Size(min = 2, max = 50, message = "Genre name must be between 2 and 50 characters")
    private String name;

    // For display purposes
    private Long bookCount;

    public GenreDto() {}

    public GenreDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public GenreDto(Long id, String name, Long bookCount) {
        this.id = id;
        this.name = name;
        this.bookCount = bookCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getBookCount() {
        return bookCount;
    }

    public void setBookCount(Long bookCount) {
        this.bookCount = bookCount;
    }

    @Override
    public String toString() {
        return "GenreDto [id=" + id + ", name=" + name + ", bookCount=" + bookCount + "]";
    }
}

