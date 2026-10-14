package com.auth.app.service.impl;

import ch.qos.logback.core.util.StringUtil;
import com.auth.app.dto.UserDto;
import com.auth.app.exception.ResourceNotFoundException;
import com.auth.app.helper.UserHelper;
import com.auth.app.model.Provider;
import com.auth.app.model.User;
import com.auth.app.repo.UserRepo;
import com.auth.app.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;

    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {

        if(StringUtil.isNullOrEmpty(userDto.getEmail())){
            throw new IllegalArgumentException("Please provide email");
        }

        if(userRepo.existsByEmail(userDto.getEmail())){
            throw new IllegalArgumentException("Email already exist");
        }

        User user = modelMapper.map(userDto, User.class);
        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);

        User savedUser = userRepo.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User with this Email not available : "+ email));

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {

        User user = userRepo.findById(UserHelper.parseUUID(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with userId !!!!"));

        if (StringUtil.isNullOrEmpty(userDto.getImage())) user.setImage(userDto.getImage());
        if (StringUtil.isNullOrEmpty(userDto.getPassword())) user.setPassword(userDto.getPassword());
        if (StringUtil.isNullOrEmpty(userDto.getName())) user.setName(userDto.getName());
        if (userDto.getProvider() != null) user.setProvider(userDto.getProvider());

        user.setEnable(userDto.isEnable());

        User savedUser = userRepo.save(user);

        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {
        UUID uuid = UserHelper.parseUUID(userId);
        User user = userRepo.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found !!"));

        userRepo.delete(user);
    }

    @Override
    public UserDto getUserById(String userId) {

        User user = userRepo.findById(UserHelper.parseUUID(userId))
                .orElseThrow(() -> new ResourceNotFoundException());
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public Iterable<UserDto> getAllUser() {
        return userRepo.findAll().stream().map(user -> modelMapper.map(user, UserDto.class)).toList();
    }
}
