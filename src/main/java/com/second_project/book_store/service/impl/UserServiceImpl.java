package com.second_project.book_store.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.second_project.book_store.entity.User;
import com.second_project.book_store.entity.User.UserRole;
import com.second_project.book_store.event.RegistrationCompleteEvent;
import com.second_project.book_store.model.UserDto;
import com.second_project.book_store.repository.UserRepository;
import com.second_project.book_store.service.UserService;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          ApplicationEventPublisher eventPublisher){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public User registerUser(UserDto userDto, String applicationUrl) {
        User user = new User();

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setAddress(userDto.getAddress());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(UserRole.USER);
        // user.setEnabled(false); default is false already

        user = userRepository.save(user);
        
        // Publish event - listeners will handle token creation, email sending, etc.
        eventPublisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl));

        return user;
    }
}
