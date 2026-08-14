package com.drs.user_service.service;

import com.drs.user_service.dto.CreateUserRequest;
import com.drs.user_service.dto.UserResponse;
import com.drs.user_service.entity.User;
import com.drs.user_service.exception.EmailAlreadyExistsException;
import com.drs.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setApiKey(UUID.randomUUID().toString());
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser) ;
    }


    public boolean isValidApiKey(String apikey){
        return userRepository.findByApiKey(apikey).isPresent();

    }

    private UserResponse mapToUserResponse(User user){
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setApiKey(user.getApiKey());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}