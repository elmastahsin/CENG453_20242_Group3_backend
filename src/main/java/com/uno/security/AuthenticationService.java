package com.uno.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uno.dtos.JwtResponse;
import com.uno.dtos.LoginRequestDTO;
import com.uno.dtos.RegisterUserDTO;
import com.uno.dtos.responseDto.GeneralResponse;
import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.dtos.responseDto.Status;
import com.uno.entity.PasswordResetToken;
import com.uno.entity.User;
import com.uno.repository.UserRepository;
import com.uno.service.EmailService;
import com.uno.util.MapperUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final MapperUtil mapper;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    public AuthenticationService(UserRepository userRepository,
                                 BCryptPasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager,
                                 JwtService jwtService,
                                 UserDetailsService userDetailsService,
                                 MapperUtil mapper,
                                 PasswordResetTokenRepository tokenRepository,
                                 EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.mapper = mapper;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    // authentication method
    public ResponseEntity<?> login(LoginRequestDTO loginRequestDTO) {
        Authentication authentication;
        try {

            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
        if (authentication == null) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
        // Set authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate access and refresh tokens
        String accessToken = jwtService.generateToken(loginRequestDTO.getUsername());
        String refreshToken = jwtService.generateRefreshToken(loginRequestDTO.getUsername());

//        UserDetailsServiceImpl userDetails = (UserDetailsServiceImpl) authentication.getPrincipal();

        //LastLogin Update USer
        User user = userRepository.findByUsername(loginRequestDTO.getUsername()).get();
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);


        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken));
    }

    // Refresh token method
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        Boolean refresh = false;
        String username = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            refresh = jwtService.extractClaim(token, claims -> claims.get("refresh", Boolean.class));
            username = jwtService.extractUsername(token);
        }
        if (refresh && username != null) {
            if (jwtService.validateRefreshToken(token)) {

                String accessToken = jwtService.generateToken(username);
                String refreshToken = jwtService.generateRefreshToken(username);

                var authResponse = new JwtResponse(accessToken, refreshToken);
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }

        new ObjectMapper().writeValue(response.getOutputStream(), "This is not RefreshToken");
        return null;
    }


    public ResponseEntity<?> register(@Valid RegisterUserDTO userDTO) {
        Optional<User> savedUser = userRepository.findByUsername((userDTO.getUsername()));
        if (savedUser.isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }
        User newUser = mapper.convert(userDTO, new User());
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        newUser.setGameCount(0L);
        newUser.setVerified(false);
        newUser.setAvatar("https://www.gravatar.com/avatar/?d=mp");
        newUser.setLastLogin(LocalDateTime.now());
        userRepository.save(newUser);
        //authenticate the user after registration
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Generate access and refresh tokens
        String accessToken = jwtService.generateToken(userDTO.getUsername());
        String refreshToken = jwtService.generateRefreshToken(userDTO.getUsername());
        // Set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Create a response object
        JwtResponse jwtResponse = new JwtResponse(accessToken, refreshToken);
        // Return the response
//        try {
//            new ObjectMapper().writeValue(new ObjectMapper().getFactory().createGenerator(response.getOutputStream()), jwtResponse);
//        } catch (IOException e) {
//            logger.error("Error writing response", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error writing response");
//        }

        return ResponseEntity.ok(new GeneralResponseWithData(new Status(HttpStatus.OK,"User register successfully"),jwtResponse));
    }

    public ResponseEntity<GeneralResponseWithData<List<User>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(new GeneralResponseWithData<List<User>>(getSuccessStatus(), users));

    }

    private ResponseEntity<GeneralResponse> getSuccessResponse() {
        return ResponseEntity.ok(new GeneralResponse(getSuccessStatus()));
    }

    private Status getSuccessStatus() {
        Status status = new Status();
        status.setCode(HttpStatus.OK);
        status.setDescription("No error");
        return status;
    }

    public ResponseEntity<?> deleteUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found!");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully!");
    }

    public ResponseEntity<?> resetPassword(String username, String password, String newPassword) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found!");
        }
        if (!passwordEncoder.matches(password, user.get().getPassword())) {
            return ResponseEntity.badRequest().body("Incorrect password!");
        }
        user.get().setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user.get());
        return ResponseEntity.ok("Password reset successfully!");
    }

    public ResponseEntity<?> createPasswordResetToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User with this email not found!");
        }
        // Check if a token already exists for this user
//        PasswordResetToken existingToken = tokenRepository.findByUserId(userOptional.get().getId());
//        if (existingToken != null && !existingToken.isExpired()) {
//            return ResponseEntity.badRequest().body("Password reset token already exists for this user please check your email");
//        }


        User user = userOptional.get();

        // Generate a random token
        String token = UUID.randomUUID().toString();

        // Create a new password reset token entity
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // Token valid for 24 hours

        // Save the token
        tokenRepository.save(resetToken);

        try {
            // Send email with reset link
            emailService.sendPasswordResetEmail(user.getEmail(), token, user.getUsername());
            return ResponseEntity.ok("Password reset link has been sent to your email");
        } catch (MailException e) {
            logger.error("Failed to send password reset email", e);
            // Return the token directly to user as fallback
            String resetLink = "Please use this token to reset your password: " + token;
            return ResponseEntity.ok(resetLink);
        }
    }

    public ResponseEntity<?> validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);

        if (tokenOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid password reset token");
        }

        PasswordResetToken resetToken = tokenOptional.get();

        // Check if token is expired
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken); // Clean up expired token
            return ResponseEntity.badRequest().body("Password reset token has expired");
        }

        return ResponseEntity.ok("Token is valid");
    }

    public ResponseEntity<?> setNewPasswordByToken(String token, String newPassword, String confirmPassword) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);

        if (tokenOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid password reset token");
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("New password and confirmation do not match");
        }
        PasswordResetToken resetToken = tokenOptional.get();

        // Check if token is expired
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken); // Clean up expired token
            return ResponseEntity.badRequest().body("Password reset token has expired");
        }

        // Get the user from the token
        User user = resetToken.getUser();

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete the used token
        tokenRepository.delete(resetToken);

        return ResponseEntity.ok("Password has been reset successfully");
    }
}
