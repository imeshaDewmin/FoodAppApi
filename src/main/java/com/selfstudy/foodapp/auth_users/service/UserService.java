package com.selfstudy.foodapp.auth_users.service;

import com.selfstudy.foodapp.auth_users.dto.UserDTO;
import com.selfstudy.foodapp.auth_users.entity.User;
import com.selfstudy.foodapp.response.Response;

import java.util.List;

public interface UserService {

    User getCurrentLoggedInUser();

    Response<List<UserDTO>> getAllUsers();

    Response<UserDTO> getOwnAccountDetails();

    Response<?> updateOwnAccount(UserDTO userDTO);

    Response<?> deactivateOwnAccount();
}
