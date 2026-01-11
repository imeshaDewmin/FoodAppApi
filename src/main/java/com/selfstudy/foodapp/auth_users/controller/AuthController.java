package com.selfstudy.foodapp.auth_users.controller;

import com.selfstudy.foodapp.auth_users.dto.LoginRequest;
import com.selfstudy.foodapp.auth_users.dto.LoginResponse;
import com.selfstudy.foodapp.auth_users.dto.RegistrationRequest;
import com.selfstudy.foodapp.auth_users.service.AuthService;
import com.selfstudy.foodapp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Response<?>> register(@RequestBody @Valid RegistrationRequest registrationRequest){
        return ResponseEntity.ok(authService.register(registrationRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest){
        authService.login(loginRequest);
        return ResponseEntity.ok(authService.login(loginRequest));
    }

}
