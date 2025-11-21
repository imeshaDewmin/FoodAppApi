package com.selfstudy.foodapp.menu.repository;

import com.selfstudy.foodapp.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MenuRepository extends JpaRepository<Menu,Long>, JpaSpecificationExecutor<Menu> {

}
