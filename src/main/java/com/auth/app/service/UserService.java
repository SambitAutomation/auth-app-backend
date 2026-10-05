package com.auth.app.service;

import com.auth.app.dto.UserDto;

import java.util.List;

public interface UserService {

    //Create User
    UserDto createUser(UserDto userDto);

    //Get User By emailId
    UserDto getUserByEmail(String email);

    //Update user
    UserDto updateUser(UserDto userDto, String UserId);

    //Delete User
    void deleteUser(String userId);

    //Get User By Id
    UserDto getUserById(String userId);

    //Get All User
    Iterable<UserDto> getAllUser();
}
