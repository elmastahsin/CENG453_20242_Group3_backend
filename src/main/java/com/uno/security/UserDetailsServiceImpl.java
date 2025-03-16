package com.uno.security;

import com.uno.dtos.RegisterUserDTO;
import com.uno.entity.User;
import com.uno.repository.UserRepository;
import com.uno.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserService userService;
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        return UserDetailsImpl.build(user);
    }


    public RegisterUserDTO getCurrentUser() {
        return userService.findByUsername(SecurityContextHolder.getContext().
                getAuthentication().getName());
    }
}
