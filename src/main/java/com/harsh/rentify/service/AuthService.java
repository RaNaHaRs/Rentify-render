package com.harsh.rentify.service;

import com.harsh.rentify.dto.request.RegisterRequest;
import com.harsh.rentify.entity.ProfileEntity;
import com.harsh.rentify.entity.Role;
import com.harsh.rentify.entity.UserEntity;
import com.harsh.rentify.exception.BusinessException;
import com.harsh.rentify.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserEntity register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Password confirmation does not match.");
        }
        if (request.getRole() == null || request.getRole() == Role.ADMIN) {
            throw new BusinessException("Choose either landlord or tenant registration.");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new BusinessException("That username is already in use.");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("That email is already in use.");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEnabled(true);
        user.setHostConfirmed(request.getRole() != Role.LANDLORD);

        ProfileEntity profile = new ProfileEntity();
        profile.setFirstName(request.getFirstName().trim());
        profile.setLastName(request.getLastName().trim());
        user.setProfile(profile);

        return userRepository.save(user);
    }
}
