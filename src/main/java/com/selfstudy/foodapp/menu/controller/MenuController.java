package com.selfstudy.foodapp.menu.controller;

import com.selfstudy.foodapp.menu.dto.MenuDto;
import com.selfstudy.foodapp.menu.service.MenuService;
import com.selfstudy.foodapp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/menu")
@RequiredArgsConstructor
public class MenuController {

    @Autowired
    private MenuService menuService;

    @PostMapping(value = "/add",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    ResponseEntity<Response<MenuDto>> createMenu(@ModelAttribute @Valid MenuDto menuDto,
                                                 @RequestPart(value = "imageFile", required = true)MultipartFile imageFile){

        menuDto.setImage(imageFile);
        return ResponseEntity.ok(menuService.createMenu(menuDto));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')")
    ResponseEntity<Response<MenuDto>> updateMenu(@ModelAttribute @Valid MenuDto menuDto,
                                                 @RequestPart(value = "imageFile", required = false)MultipartFile imageFile) {

        menuDto.setImage(imageFile);
        return ResponseEntity.ok(menuService.updateMenu(menuDto));
    }

    @GetMapping("/{id}")
    ResponseEntity<Response<MenuDto>> getMenuById(@PathVariable Long id){
        return ResponseEntity.ok(menuService.getMenuById(id));
    }

    @GetMapping()
    ResponseEntity<Response<List<MenuDto>>> getMenuList(@RequestParam(required = false) Long categoryId, @RequestParam(required = false) String search){
        return ResponseEntity.ok(menuService.getMenus(categoryId,search));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ResponseEntity<Response<?>> deleteMenu(@PathVariable Long id){
        return ResponseEntity.ok(menuService.deleteMenu(id));
    }
}
