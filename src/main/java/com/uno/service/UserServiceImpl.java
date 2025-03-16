package com.uno.service;

import com.uno.dtos.RegisterUserDTO;
import com.uno.entity.User;
import com.uno.repository.UserRepository;
import com.uno.util.MapperUtil;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final MapperUtil mapper;

    public UserServiceImpl(UserRepository userRepository, MapperUtil mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }


    @Override
    public RegisterUserDTO findByUsername(String name) {
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + name));
        return mapper.convert(user, new RegisterUserDTO());
    }
}
