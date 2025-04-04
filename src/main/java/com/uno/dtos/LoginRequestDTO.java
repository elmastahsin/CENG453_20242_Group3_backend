package com.uno.dtos;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
public class LoginRequestDTO {

    private String username;
    private String password;
}
