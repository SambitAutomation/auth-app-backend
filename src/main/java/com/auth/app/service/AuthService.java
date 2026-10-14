package com.auth.app.service;

import com.auth.app.dto.UserDto;

public interface AuthService {
    UserDto registerUser(UserDto userDto);
}
