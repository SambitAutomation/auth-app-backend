package com.auth.app.controller;

import com.auth.app.dto.LoginRequest;
import com.auth.app.dto.TokenResponse;
import com.auth.app.dto.UserDto;
import com.auth.app.model.RefreshToken;
import com.auth.app.model.User;
import com.auth.app.repo.RefreshTokenRepo;
import com.auth.app.repo.UserRepo;
import com.auth.app.security.CookieService;
import com.auth.app.security.JwtService;
import com.auth.app.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final AuthenticationManager authManager;

    private  final UserRepo userRepo;

    private final JwtService jwtService;

    private final ModelMapper modelMapper;

    private final RefreshTokenRepo refreshTokenRepo;

    private final CookieService cookieService;



    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response){
        
        Authentication auth =  authenticate(loginRequest);
        User user = userRepo.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BadCredentialsException("Username or password is not valid!!!"));
        if(!user.isEnable()){
            throw new DisabledException("User is disabled");
        }


        String jti = UUID.randomUUID().toString();
        var refershTokenObj = RefreshToken.builder()
                .jit(jti)
                .user(user)
                .createdAt(Instant.now())
                .updatedAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSecond()))
                .revoked(false)
                .build();

        refreshTokenRepo.save(refershTokenObj);

        //Access Token -- generate
        String accessToken = jwtService.generateToken(user);
        //Refresh Token -- generate
        String refreshToken = jwtService.generateRefreshToken(user, refershTokenObj.getJit());

        //Adding up Refresh Token under Cookies
        cookieService.attachRefreshCookie(response, refreshToken, (int)jwtService.getRefreshTtlSecond());
        cookieService.addNoStoreHeaders(response);

        TokenResponse tokenResponse =  TokenResponse.of(accessToken,refreshToken, jwtService.getAccessTtlSecond(),
                modelMapper.map(user, UserDto.class));

        return ResponseEntity.ok(tokenResponse);

    }

    private Authentication authenticate(LoginRequest loginRequest) {

        try{
             return  authManager.authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.email(), loginRequest.password()
                ));
        } catch (Exception e) {
            throw  new BadCredentialsException("Username or password is not valid!!");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }
}
