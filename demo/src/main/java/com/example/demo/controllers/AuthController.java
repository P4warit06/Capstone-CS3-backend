package com.example.demo.controllers;

import com.example.demo.dtos.AuthResponseDto;
import com.example.demo.entity.User;
import com.example.demo.security.JwtProvider;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.dtos.UserDto;
import com.example.demo.services.auth.AuthService;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtProvider jwtProvider;

    public AuthController(AuthService authService, JwtProvider jwtProvider) {
        this.authService = authService;
        this.jwtProvider = jwtProvider;
    }
    /** Called once by the frontend right after Microsoft login. Header: Authorization: Bearer {id_token} */
    @PostMapping("/sync")
    public AuthResponseDto sync(@AuthenticationPrincipal Jwt token) {
        User user = authService.sync(token);
        String backendJwtToken = jwtProvider.generateToken(user);
        return new AuthResponseDto(backendJwtToken, UserDto.from(user));
    }

    @GetMapping("/profile")
    public UserDto profile(@AuthenticationPrincipal Jwt token) {
        return UserDto.from(authService.getProfile(token));
    }
}
