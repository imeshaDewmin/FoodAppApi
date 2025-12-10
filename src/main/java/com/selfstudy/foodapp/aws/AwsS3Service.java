package com.selfstudy.foodapp.aws;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;


public interface AwsS3Service {
    URL uploadFile(String keyName, MultipartFile multipartFile);
    void deleteFile(String keyName);
}
