package com.selfstudy.foodapp.category.repository;

import com.selfstudy.foodapp.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {

}
