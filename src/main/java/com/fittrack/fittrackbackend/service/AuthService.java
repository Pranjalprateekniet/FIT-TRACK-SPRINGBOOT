package com.fittrack.fittrackbackend.service;

import com.fittrack.fittrackbackend.dto.LoginRequest;
import com.fittrack.fittrackbackend.dto.RegisterRequest;
import com.fittrack.fittrackbackend.entity.User;
import com.fittrack.fittrackbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already exists";
        }
        User user = User.builder()
        .name(request.getName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .createdAt(LocalDateTime.now())
        .build();

        userRepository.save(user);
        return "User registered successfully";
    }
    public String login(LoginRequest request){
            User user=userRepository.findByEmail(request.getEmail()).orElseThrow(()->new RuntimeException("User not found"));

            boolean isPasswordCorrect=passwordEncoder.matches(request.getPassword(), user.getPassword());
            if(!isPasswordCorrect){
                throw  new RuntimeException("Incorrect password");
            }
            String token= jwtService.generateToken(user.getEmail());
            return token;
    }
}
