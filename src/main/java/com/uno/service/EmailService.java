package com.uno.service;

public interface EmailService {
    void sendPasswordResetEmail(String email, String token);
}
