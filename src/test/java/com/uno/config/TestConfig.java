package com.uno.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
class TestConfig {
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        // A simple test implementation
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return "TEST_ENCODED:" + rawPassword;
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return ("TEST_ENCODED:" + rawPassword).equals(encodedPassword);
            }
        };
    }
}