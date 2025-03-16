package com.uno.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uno.dtos.JwtResponse;
import com.uno.dtos.LoginRequestDTO;
import com.uno.dtos.RegisterUserDTO;
import com.uno.dtos.responseDto.GeneralResponse;
import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.dtos.responseDto.Status;
import com.uno.entity.User;
import com.uno.repository.UserRepository;
import com.uno.util.MapperUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final MapperUtil mapper;


    public AuthenticationService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService, UserDetailsService userDetailsService, MapperUtil mapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.mapper = mapper;
    }

    // authentication method
    public ResponseEntity<?> login(LoginRequestDTO loginRequestDTO) {
        Authentication authentication;
        try {

            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword()));
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
    public ResponseEntity<?> refreshToken(HttpServletRequest request,
                                          HttpServletResponse response) throws IOException {
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

                var authResponse = new JwtResponse(accessToken,
                        refreshToken);
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
        userRepository.save(newUser);
        return ResponseEntity.ok("user registered successfully!");
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
        status.setCode("200");
        status.setDescription("No error");
        return status;
    }

    public ResponseEntity<?> deleteUser(UUID id) {
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
}
