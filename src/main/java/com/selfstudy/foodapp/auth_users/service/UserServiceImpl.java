package com.selfstudy.foodapp.auth_users.service;


import com.selfstudy.foodapp.auth_users.dto.UserDTO;
import com.selfstudy.foodapp.auth_users.entity.User;
import com.selfstudy.foodapp.auth_users.repository.UserRepository;
import com.selfstudy.foodapp.aws.AwsS3Service;
import com.selfstudy.foodapp.email_notification.dto.NotificationDto;
import com.selfstudy.foodapp.email_notification.service.NotificationService;
import com.selfstudy.foodapp.exceptions.NotFoundException;
import com.selfstudy.foodapp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AwsS3Service awsS3Service;


    @Override
    public User getCurrentLoggedInUser() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return
        userRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundException("User not found"));
    }

    @Override
    public Response<List<UserDTO>> getAllUsers() {

        log.info("Inside getAllUsers()");

        List<User> allUsers = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<UserDTO> userDto = modelMapper.map(allUsers, new TypeToken<List<UserDTO>>(){}.getType());

        return Response.<List<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("All users retrieved")
                .data(userDto)
                .build();
    }

    @Override
    public Response<UserDTO> getOwnAccountDetails() {
        log.info("Inside getOwnAccountDetails()");

        User user = getCurrentLoggedInUser();

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return Response.<UserDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Own account details retrieved")
                .data(userDTO)
                .build();
    }

    @Override
    public Response<?> updateOwnAccount(UserDTO userDTO) {
        log.info("Inside updateOwnAccount()");

        User user = getCurrentLoggedInUser();

        String profileUrl = user.getProfileUrl();

        MultipartFile imageFile = userDTO.getImageFile();

        //check if the image was provided

        if(imageFile != null && !imageFile.isEmpty()){
            //delete old image first
            if(profileUrl != null && !profileUrl.isEmpty()){
                String keyName = profileUrl.substring(profileUrl.lastIndexOf("/") + 1);

                awsS3Service.deleteFile("profile/"+ keyName);
                log.info("Deleting old DP from s3");
            }
            //upload the new image
            String imageName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            URL newImageUrl = awsS3Service.uploadFile("profile/" + imageName,imageFile);
            user.setProfileUrl(newImageUrl.toString());

        }

        //update other details
        if(userDTO.getName()!= null)user.setName(userDTO.getName());
        if(userDTO.getAddress()!= null)user.setAddress(userDTO.getAddress());
        if(userDTO.getPhoneNumber()!= null)user.setPhoneNumber(userDTO.getPhoneNumber());
        if(userDTO.getPassword()!= null) user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        if(userDTO.getEmail()!=null && !userDTO.getEmail().equals(user.getEmail()))user
                .setEmail(userDTO.getEmail());

        userRepository.save(user);
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("User update success")
                .build();
    }

    @Override
    public Response<?> deactivateOwnAccount() {
        log.info("Inside deactivateOwnAccount()");

        User user = getCurrentLoggedInUser();

        user.setActive(false);
        userRepository.save(user);

        NotificationDto notificationDto = NotificationDto.builder()
                .recipient(user.getEmail())
                .subject("Account deactivated")
                .body("Your account has been deactivated. Please contact support if this was a mistake")
                .build();

        notificationService.sendEmail(notificationDto);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Deactivate success")
                .build();
    }
}
