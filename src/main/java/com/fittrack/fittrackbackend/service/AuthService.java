package com.fittrack.fittrackbackend.service;

import com.fittrack.fittrackbackend.dto.LoginRequest;
import com.fittrack.fittrackbackend.dto.RegisterRequest;
import com.fittrack.fittrackbackend.entity.AuthProvider;
import com.fittrack.fittrackbackend.entity.User;
import com.fittrack.fittrackbackend.entity.VerificationToken;
import com.fittrack.fittrackbackend.repository.UserRepository;
import com.fittrack.fittrackbackend.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.translate.UnicodeUnescaper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;



    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already exists";
        }
        User user = User.builder()
        .name(request.getName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .createdAt(LocalDateTime.now())
        .emailVerified(false)
        .authProvider(AuthProvider.LOCAL)
        .build();

        User saveduser=userRepository.save(user);
        String token= UUID.randomUUID().toString();

        VerificationToken verificationToken=VerificationToken.builder()
                .token(token)
                .user(saveduser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(verificationToken);

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

    public String verifyEmail(String token){
            VerificationToken verificationToken=verificationTokenRepository.findByToken(token).orElseThrow(()->new RuntimeException("Invalid verification token"));
            if(LocalDateTime.now().isAfter(verificationToken.getExpiryDate())){
                throw new RuntimeException("Verification token expired");
            }
            User user=verificationToken.getUser();
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(LocalDateTime.now());
            userRepository.save(user);
            verificationTokenRepository.delete(verificationToken);
            return "Email verified successfully";
    }
}
