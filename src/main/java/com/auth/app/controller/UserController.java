package com.auth.app.controller;

import com.auth.app.dto.UserDto;
import com.auth.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto){
        return  ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }

    @GetMapping
    public  ResponseEntity<Iterable<UserDto>> findAllUser(){
        return  ResponseEntity.ok(userService.getAllUser());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> findUserByEmail(@PathVariable String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @DeleteMapping("/{userId}")
    public  void deleteUserById(@PathVariable String userId){
         userService.deleteUser(userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto, @PathVariable String userId){
        return ResponseEntity.ok(userService.updateUser(userDto, userId));
    }
}
