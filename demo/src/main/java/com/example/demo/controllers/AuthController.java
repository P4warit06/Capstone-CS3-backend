package com.example.demo.controllers;

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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Called once by the frontend right after Microsoft login. Header: Authorization: Bearer {id_token} */
    @PostMapping("/sync")
    public UserDto sync(@AuthenticationPrincipal Jwt token) {
        return UserDto.from(authService.sync(token));
    }

    @GetMapping("/profile")
    public UserDto profile(@AuthenticationPrincipal Jwt token) {
        return UserDto.from(authService.getProfile(token));
    }
}
