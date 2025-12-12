package com.selfstudy.foodapp.auth_users.controller;

import com.selfstudy.foodapp.auth_users.dto.UserDTO;
import com.selfstudy.foodapp.auth_users.service.UserService;
import com.selfstudy.foodapp.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<List<UserDTO>>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/account")
    public ResponseEntity<Response<UserDTO>> getMyAccountDetails(){
        return ResponseEntity.ok(userService.getOwnAccountDetails());
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<?>> updateOwnAccount(@ModelAttribute UserDTO userDTO,
                                                        @RequestPart (value = "imageFile", required=false)
                                                        MultipartFile imageFile){

        userDTO.setImageFile(imageFile);
        return ResponseEntity.ok(userService.updateOwnAccount(userDTO));
    }

    @PutMapping("/deactivate")
    public ResponseEntity<Response<?>> deactivateMyAccount(){
        return ResponseEntity.ok(userService.deactivateOwnAccount());
    }
}
