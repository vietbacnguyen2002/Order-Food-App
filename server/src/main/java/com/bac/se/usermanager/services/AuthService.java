package com.bac.se.usermanager.services;

import com.bac.se.usermanager.dto.request.ForgotPasswordRequest;
import com.bac.se.usermanager.dto.request.LoginRequest;
import com.bac.se.usermanager.dto.request.RegisterUserRequest;
import com.bac.se.usermanager.dto.response.AuthResponse;
import com.bac.se.usermanager.dto.response.MailBody;
import com.bac.se.usermanager.enums.Role;
import com.bac.se.usermanager.exceptions.*;
import com.bac.se.usermanager.models.User;
import com.bac.se.usermanager.repositories.UserRepository;
import com.bac.se.usermanager.utils.GenerateOTP;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final GenerateOTP generateOTP;
    private final EmailService emailService;
    private static final String ACCESS_SECRET_KEY = System.getenv("ACCESS_SECRET_KEY");
    private static final String REFRESH_SECRET_KEY = System.getenv("REFRESH_SECRET_KEY");
    private static final long ACCESS_EXPIRED = Long.parseLong(System.getenv("ACCESS_TOKEN_EXPIRED"));
    private static final long REFRESH_EXPIRED = Long.parseLong(System.getenv("REFRESH_TOKEN_EXPIRED"));

    private AuthResponse getAuthResponse(User saveUser) {
        String accessToken = jwtService.generateToken(new HashMap<>(), saveUser, ACCESS_SECRET_KEY, ACCESS_EXPIRED);
        String refreshToken = jwtService.generateToken(new HashMap<>(), saveUser, REFRESH_SECRET_KEY, REFRESH_EXPIRED);
        redisTemplate.opsForValue().set(saveUser.getId().toString(), refreshToken, Duration.of(REFRESH_EXPIRED, ChronoUnit.SECONDS));
        return new AuthResponse(accessToken, refreshToken);
    }

    public String register(RegisterUserRequest userRequest) {
        // Check if email or password is empty
        if (userRequest.email().isEmpty()
                || userRequest.password().isEmpty()
                || userRequest.confirmPassword().isEmpty()) {
            throw new UserBadRequest("Email or password are empty");
        }
        if(!userRequest.password().equals(userRequest.confirmPassword())){
            throw new UserBadRequest("Passwords do not match");
        }
        // Check if the email already exists
        if (userRepository.getUserByEmail(userRequest.email()).isPresent()) {
            throw new UserExistException("Email already in use");
        }

        try {
            // Create the new user object
            var user = User.builder()
                    .email(userRequest.email())
                    .password(passwordEncoder.encode(userRequest.password()))
                    .role(Role.USER)
                    .build();
            // Save the new user
            userRepository.save(user);
            // Generate and send OTP
            int otp = generateOTP.otpGenerator();
            emailService.sendSimpleMessage(
                    new MailBody(
                            userRequest.email(),
                            "This is email valid email register",
                            "Your account registration OTP is " + otp
                    )
            );
            return "Register Successfully";
        } catch (MailException e) {
            // Log and handle email sending failure
            log.error("Failed to send email: {}", e.getMessage());
            throw new UserBadRequest("Failed to send registration email");
        } catch (Exception e) {
            // Log and handle any other unexpected errors
            log.error("Registration error: {}", e.getMessage());
            throw new UserBadRequest("An unexpected error occurred during registration");
        }
    }


    public AuthResponse login(LoginRequest loginRequest) {
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );
            // Retrieve the user details after successful authentication
            var user = userRepository.getUserByEmail(loginRequest.username())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            log.info("Status of login is {}", authenticate.isAuthenticated());
            return getAuthResponse(user);
        } catch (AuthenticationException e) {
            // Log and handle authentication failure
            log.info("Username or password is not correct");
            throw new ForbiddenException("Username or password is not correct");
        }
    }

    public AuthResponse refreshToken(String token) {
        try {
            String username = jwtService.extractUsername(token, REFRESH_SECRET_KEY);
            if (username == null) {
                throw new UserNotFoundException("User not found");
            }
            var user = userRepository.getUserByEmail(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            if (redisTemplate.opsForValue().get(user.getId().toString()) == null) {
                throw new UserNotFoundException("User not found");
            }
            return getAuthResponse(user);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new UserBadRequest("Invalid refresh token");
        }
    }

    public String validOTP(String otp, String email) {
        try {
            if (redisTemplate.opsForValue().get(email) == null) {
                throw new OtpException("OTP expired !");
            }
            if (!otp.equals(redisTemplate.opsForValue().get(email))) {
                throw new OtpException("Invalid OTP !");
            }
            userRepository.updateValidEmail(email);
            return "OTP successfully validated";
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new OtpException(e.getMessage());
        }
    }

    public String forgotPassword(String email) throws MessagingException, FileNotFoundException {
        try {
            var user = userRepository.getUserByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            int otp = generateOTP.otpGenerator();
            MailBody mailBody = new MailBody(
                    user.getEmail(),
                    "This is the OTP for your Forgot Password request",
                    "OTP for Forgot Password request " + otp
            );
            emailService.sendSimpleMessage(mailBody);
            return "OTP code has been sent successfully";
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new UserBadRequest(ex.getMessage());
        }
    }

    public String changPassword(ForgotPasswordRequest passwordRequest, String email) {
        try {
            String passwordEncode = passwordEncoder.encode(passwordRequest.newPassword());
            log.info("Encode password is {}", passwordEncode);
            userRepository.updatePassword(email, passwordEncode);
            return "Password successfully updated";
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new UserBadRequest(ex.getMessage());
        }
    }
}
