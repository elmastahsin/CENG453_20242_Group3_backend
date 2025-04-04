package com.uno.controller;

import com.uno.dtos.LoginRequestDTO;
import com.uno.dtos.RegisterUserDTO;
import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.entity.User;
import com.uno.security.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Register User", description = "Register User")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDTO registerUserDTO) {
        ResponseEntity<?> register = authenticationService.register(registerUserDTO);
        return ResponseEntity.ok(register);
    }

    @Operation(summary = "Authenticate User", description = "Authenticate User")
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        ResponseEntity<?> login = authenticationService.login(loginRequestDTO);
        return ResponseEntity.ok(login);
    }

    @Operation(summary = "Refresh Token", description = "Refresh Token")
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request,
                                          HttpServletResponse response) throws IOException {
        ResponseEntity<?> refresh = authenticationService.refreshToken(request, response);
        return ResponseEntity.ok(refresh);
    }

    @Operation(summary = "Get All Users", description = "Get All Users")
    @GetMapping("/users")
    public ResponseEntity<GeneralResponseWithData<List<User>>> getAllUsers() {
        return authenticationService.getAllUsers();
    }

    @Operation(summary = "Delete User By Id", description = "Delete User By Id")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestParam Long id) {
        return authenticationService.deleteUser(id);
    }

    @Operation(summary = "Reset Password", description = "Reset Password")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String username, @RequestParam String password, @RequestParam String newPassword) {
        return authenticationService.resetPassword(username, password, newPassword);
    }

    @Operation(summary = "Request Password Reset", description = "Request a password reset link via email")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        return authenticationService.createPasswordResetToken(email);
    }

    @Operation(summary = "Validate Reset Token", description = "Validate the password reset token")
    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        return authenticationService.validatePasswordResetToken(token);
    }

    @Operation(summary = "Set New Password", description = "Set a new password using reset token")
    @PostMapping("/set-new-password")
    public ResponseEntity<?> setNewPassword(@RequestParam String token, @RequestParam String newPassword) {
        return authenticationService.setNewPasswordByToken(token, newPassword);
    }
}
