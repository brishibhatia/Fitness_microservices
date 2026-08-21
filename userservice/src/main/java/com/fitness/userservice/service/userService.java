package com.fitness.userservice.service;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponseDto;
import com.fitness.userservice.model.User;
import com.fitness.userservice.repository.userRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class userService {

     private final userRepository userRepository;


    public UserResponseDto getUserProfile(String userId) {
       User user =  userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
       UserResponseDto userResponseDto = new UserResponseDto();
       userResponseDto.setId(user.getId());
       userResponseDto.setFirstName(user.getFirstName());
       userResponseDto.setLastName(user.getLastName());
       userResponseDto.setEmail(user.getEmail());
       userResponseDto.setCreatedAt(user.getCreatedAt());
       userResponseDto.setUpdatedAt(user.getUpdatedAt());
       return userResponseDto;

    }

    public UserResponseDto register(RegisterRequest registerRequest) {
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            User existingUser = userRepository.findByEmail(registerRequest.getEmail());
            UserResponseDto userResponseDto = new UserResponseDto();
            userResponseDto.setId(existingUser.getId());
            userResponseDto.setFirstName(existingUser.getFirstName());
            userResponseDto.setLastName(existingUser.getLastName());
            userResponseDto.setEmail(existingUser.getEmail());
            userResponseDto.setCreatedAt(existingUser.getCreatedAt());
            userResponseDto.setUpdatedAt(existingUser.getUpdatedAt());
        }
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        User savedUser = userRepository.save(user);
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(savedUser.getId());
        userResponseDto.setEmail(savedUser.getEmail());
        userResponseDto.setFirstName(savedUser.getFirstName());
        userResponseDto.setLastName(savedUser.getLastName());
        userResponseDto.setCreatedAt(savedUser.getCreatedAt());
        userResponseDto.setUpdatedAt(savedUser.getUpdatedAt());

        return userResponseDto;
    }

    public Boolean validate(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()){
            return true;
        }
        return false;

    }
}
