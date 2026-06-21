package com.fittrack.fittrackbackend.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${resend.api.key}")
    private String apikey;
    public void sendVerificationEmail(String to,String token){
        try {
            Resend resend = new Resend(apikey);
            String verificationLink = "http://localhost:8080/api/auth/verify-email?token=" + token;
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Nutrinova <onboarding@resend.dev>")
                    .to(to)
                    .subject("Verify your Nutri Nova account")
                    .html("""
                            <h2>Welcome to Nutri Nova</h2>
                            
                            <p>Thank you for registering.</p>
                            
                            <p>Please verify your email by clicking the link below:</p>
                            
                            <a href="%s">Verify Email</a>
                            
                            <p>This link will expire in 24 hours.</p>
                            """.formatted(verificationLink))
                    .build();
            resend.emails().send(params);
        }
        catch (ResendException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
