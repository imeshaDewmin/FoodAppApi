package com.selfstudy.foodapp.auth_users.service;

import com.selfstudy.foodapp.auth_users.dto.LoginRequest;
import com.selfstudy.foodapp.auth_users.dto.LoginResponse;
import com.selfstudy.foodapp.auth_users.dto.RegistrationRequest;
import com.selfstudy.foodapp.auth_users.entity.User;
import com.selfstudy.foodapp.auth_users.repository.UserRepository;
import com.selfstudy.foodapp.exceptions.BadRequestException;
import com.selfstudy.foodapp.exceptions.NotFoundException;
import com.selfstudy.foodapp.response.Response;
import com.selfstudy.foodapp.role.entity.Role;
import com.selfstudy.foodapp.role.repository.RoleRepository;
import com.selfstudy.foodapp.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Response<?> register(RegistrationRequest registrationRequest) {
        log.info("Inside Register()");

        if (userRepository.existsByEmail(registrationRequest.getEmail())){
            throw new BadRequestException("Email already exists");

        }

        //get all roles from the request

        List<Role> userRoles;

        if (registrationRequest.getRoles() != null && !registrationRequest.getRoles().isEmpty()) {
            userRoles = registrationRequest.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName.toUpperCase())
                            .orElseThrow(() -> new NotFoundException("Role not found")))
                    .toList();
        } else {
            Role defaultRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new NotFoundException("CUSTOMER role not found"));
            userRoles = List.of(defaultRole);
        }

        //build a user object

        User userToSave = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .address(registrationRequest.getAddress())
                .phoneNumber(registrationRequest.getPhoneNumber())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .roles(userRoles)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        userRepository.save(userToSave);

        log.info("User registered successfully");

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("User registered successfully")
                .build();

    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {

        log.info("Inside login ()");

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new NotFoundException("Invalid email"));

        if(!user.isActive()){
            throw new NotFoundException("User is inactive..Please contact customer support");
        }
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new BadRequestException("Invalid password");
        }

        //generate a token

        String token = jwtUtils.generateJwtToken(user.getEmail());

        //extract role names as a list

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setRoles(roles);

        return
                Response.<LoginResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Login success")
                        .data(loginResponse)
                        .build();
    }
}
