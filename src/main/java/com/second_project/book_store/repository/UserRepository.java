package com.second_project.book_store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.second_project.book_store.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds user by email (case-sensitive).
     * 
     * @param email Email address
     * @return Optional containing User if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds user by email (case-insensitive).
     * BEST PRACTICE: Use this for authentication to handle case variations.
     * Example: "user@example.com" and "User@Example.com" are treated as the same.
     * 
     * @param email Email address (case-insensitive)
     * @return Optional containing User if found
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Finds user by role.
     * 
     * @param role User role
     * @return Optional containing User if found
     */
    Optional<User> findByRole(User.UserRole role);
}
