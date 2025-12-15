package com.selfstudy.foodapp.category.service;

import com.selfstudy.foodapp.category.dto.CategoryDto;
import com.selfstudy.foodapp.category.entity.Category;
import com.selfstudy.foodapp.category.repository.CategoryRepository;
import com.selfstudy.foodapp.exceptions.NotFoundException;
import com.selfstudy.foodapp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public Response<CategoryDto> addCategory(CategoryDto categoryDto) {
        log.info("Inside addCategory()");
        Category category = modelMapper.map(categoryDto,Category.class);

        categoryRepository.save(category);

        return Response.<CategoryDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Category added success")
                .build();

    }

    @Override
    public Response<CategoryDto> getCategoryById(Long id) {
        log.info("Inside getCategoryById()");

        Category category = categoryRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Category not found"));

        CategoryDto categoryDto = modelMapper.map(category,CategoryDto.class);

        return Response.<CategoryDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Category retrieved")
                .data(categoryDto)
                .build();
    }

    @Override
    public Response<CategoryDto> updateCategory(CategoryDto categoryDto) {
        log.info("Inside updateCategory()");

        Category category = categoryRepository.findById(categoryDto.getId())
                .orElseThrow(()-> new NotFoundException("Category not found"));

        if(categoryDto.getName() != null && !categoryDto.getName().isEmpty())
            category.setName(categoryDto.getName());

        if(categoryDto.getDescription() != null && !categoryDto.getDescription().isEmpty())
            category.setDescription(categoryDto.getDescription());

        categoryRepository.save(category);

        return Response.<CategoryDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Category update success")
                .build();

    }

    @Override
    public Response<List<CategoryDto>> getAllCategories() {
        log.info("Inside getAllCategories()");

        List<Category> allCategories = categoryRepository.findAll();

        List<CategoryDto> categoryDtos = allCategories.stream()
                .map(category ->modelMapper.map(category,CategoryDto.class))
                .toList();

        return
        Response.<List<CategoryDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Categories retrieved success")
                .data(categoryDtos)
                .build();
    }

    @Override
    public Response<?> deleteCategory(Long id) {
        log.info("Inside deleteCategory()");

        Category category = categoryRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Category not found"));

        categoryRepository.deleteById(category.getId());

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Category deleted")
                .build();
    }
}
