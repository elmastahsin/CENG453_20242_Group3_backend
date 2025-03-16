package com.uno.service;

import com.uno.dtos.RegisterUserDTO;

public interface UserService {
    RegisterUserDTO findByUsername(String name);
}
