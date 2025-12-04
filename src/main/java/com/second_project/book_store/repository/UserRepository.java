package com.second_project.book_store.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * Find user by phone number
     * @param phoneNumber String phone number
     * @return Optional containing user if found
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Finds a user by role.
     *
     * @param role User role
     * @return Optional containing User if found
     */
    Optional<User> findByRole(User.UserRole role);

    /**
     * Counts users by role.
     * Used to ensure at least one ADMIN user always exists.
     *
     * @param role User role
     * @return Number of users with the given role
     */
    long countByRole(User.UserRole role);

    /**
     * Count users by enabled status.
     * Used for dashboard statistics to count verified users.
     * 
     * @param isEnabled Enabled status (true for verified/enabled users)
     * @return Count of users with given status
     */
    Long countByIsEnabled(boolean isEnabled);

    /**
     * Searches users by email, first name, or last name containing the given keyword
     * (case-insensitive).
     *
     * @param emailKeyword     Keyword for email
     * @param firstNameKeyword Keyword for first name
     * @param lastNameKeyword  Keyword for last name
     * @param pageable         Pagination information
     * @return Page of users matching the keyword
     */
    Page<User> findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String emailKeyword,
            String firstNameKeyword,
            String lastNameKeyword,
            Pageable pageable);
}
