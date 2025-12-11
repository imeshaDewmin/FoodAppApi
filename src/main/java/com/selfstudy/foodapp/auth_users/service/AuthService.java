package com.selfstudy.foodapp.auth_users.service;

import com.selfstudy.foodapp.auth_users.dto.LoginRequest;
import com.selfstudy.foodapp.auth_users.dto.LoginResponse;
import com.selfstudy.foodapp.auth_users.dto.RegistrationRequest;
import com.selfstudy.foodapp.response.Response;

public interface AuthService {

    Response<?> register(RegistrationRequest registrationRequest);
    Response<LoginResponse>login(LoginRequest loginRequest);

}
