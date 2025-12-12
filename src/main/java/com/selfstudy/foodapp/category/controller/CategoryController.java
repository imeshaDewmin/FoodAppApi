package com.selfstudy.foodapp.category.controller;

import com.selfstudy.foodapp.category.dto.CategoryDto;
import com.selfstudy.foodapp.category.service.CategoryService;
import com.selfstudy.foodapp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/categories")
@RequiredArgsConstructor
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    ResponseEntity<Response<CategoryDto>>addCategory(@RequestBody @Valid CategoryDto categoryDto){
        return ResponseEntity.ok(categoryService.addCategory(categoryDto));
    }

    @GetMapping("/{id}")
    ResponseEntity<Response<CategoryDto>>getCategoryById(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')")
    ResponseEntity<Response<CategoryDto>>updateCategory(@RequestBody CategoryDto categoryDto){
        return ResponseEntity.ok(categoryService.updateCategory(categoryDto));
    }

    @GetMapping("/all")
    ResponseEntity<Response<List<CategoryDto>>>getAllCategories(){
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ResponseEntity<Response<?>>deleteCategory(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }
}
