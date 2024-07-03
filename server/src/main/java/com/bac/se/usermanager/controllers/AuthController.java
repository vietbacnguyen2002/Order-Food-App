package com.bac.se.usermanager.controllers;

import com.bac.se.usermanager.dto.request.ForgotPasswordRequest;
import com.bac.se.usermanager.dto.request.LoginRequest;
import com.bac.se.usermanager.dto.request.RegisterUserRequest;
import com.bac.se.usermanager.dto.response.AuthResponse;
import com.bac.se.usermanager.dto.response.MailBody;
import com.bac.se.usermanager.models.User;
import com.bac.se.usermanager.repositories.UserRepository;
import com.bac.se.usermanager.services.AuthService;
import com.bac.se.usermanager.services.EmailService;
import com.bac.se.usermanager.utils.GenerateOTP;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final GenerateOTP generateOTP;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> registerUser(@RequestBody RegisterUserRequest userRequest) throws MessagingException, FileNotFoundException {
        return ResponseEntity.ok(authService.register(userRequest));
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse loginAccount(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }


    @PostMapping("/forgot/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> sendEmail(@PathVariable("email") String email) throws MessagingException, FileNotFoundException {
        return ResponseEntity.ok(authService.forgotPassword(email));
    }


    @PostMapping("/refreshToken/{token}")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse refreshToken(@PathVariable("token") String refreshToken) {
        return authService.refreshToken(refreshToken);
    }

    @PostMapping("/validOTP/{email}/{otp}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> validOTP(@PathVariable("email") String email,
                                           @PathVariable("otp") String otp) {
        String status = authService.validOTP(otp, email);
        return ResponseEntity.ok(status);
    }


    @PostMapping("/changePassword/{email}")
    public ResponseEntity<String> changePassword(@RequestBody ForgotPasswordRequest passwordRequest, @PathVariable("email") String email) {
        if (!passwordRequest.newPassword().equals(passwordRequest.confirmPassword())) {
            return new ResponseEntity<>("Passwords do not match", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(authService.changPassword(passwordRequest, email));
    }
}
