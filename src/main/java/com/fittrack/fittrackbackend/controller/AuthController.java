package com.fittrack.fittrackbackend.controller;

import com.fittrack.fittrackbackend.dto.JwtResponse;
import com.fittrack.fittrackbackend.dto.LoginRequest;
import com.fittrack.fittrackbackend.dto.RegisterRequest;
import com.fittrack.fittrackbackend.dto.TokenRefreshRequest;
import com.fittrack.fittrackbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request){

        return authService.register(request);
    }
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request){
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request){
        JwtResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token){
        String response=authService.verifyEmail(token);
        return response;
    }
}
