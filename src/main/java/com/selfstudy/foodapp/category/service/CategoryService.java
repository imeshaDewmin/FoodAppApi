package com.selfstudy.foodapp.category.service;

import com.selfstudy.foodapp.category.dto.CategoryDto;
import com.selfstudy.foodapp.response.Response;

import java.util.List;

public interface CategoryService {

    Response<CategoryDto> addCategory(CategoryDto categoryDto);

    Response<CategoryDto> getCategoryById(Long id);

    Response<CategoryDto> updateCategory(CategoryDto categoryDto);

    Response<List<CategoryDto>> getAllCategories();

    Response<?>deleteCategory(Long id);

}
