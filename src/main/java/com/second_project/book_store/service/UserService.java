package com.second_project.book_store.service;

import com.second_project.book_store.entity.User;
import com.second_project.book_store.model.UserDto;

public interface UserService {

    User registerUser(UserDto userDto, String applicationUrl);
    
    void resendVerificationToken(String email, String applicationUrl);
}
