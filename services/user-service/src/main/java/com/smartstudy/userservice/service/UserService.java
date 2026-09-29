package com.smartstudy.userservice.service;

import com.smartstudy.userservice.dto.LoginRequest;
import com.smartstudy.userservice.dto.UserProfileResponse;
import com.smartstudy.userservice.exception.AuthenticationException;
import com.smartstudy.userservice.exception.DuplicateEmailException;
import com.smartstudy.userservice.model.User;
import com.smartstudy.userservice.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtService = jwtService;
    }

    public User register(User user) {

        // An email should belong to only one account.
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email is already registered");
        }

        // Never store the original password in the database.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new AuthenticationException("Invalid email or password"));

        // Compare the entered password with the BCrypt hash stored in PostgreSQL.
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new AuthenticationException("Invalid email or password");
        }

        // Generate a signed JWT after successful authentication.
        return jwtService.generateToken(user.getId(), user.getEmail());
    }

    public UserProfileResponse getProfile(String token) {

        if (!jwtService.isTokenValid(token)) {
            throw new AuthenticationException("Invalid or expired token");
        }

        String email = jwtService.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AuthenticationException("Invalid or expired token"));

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}