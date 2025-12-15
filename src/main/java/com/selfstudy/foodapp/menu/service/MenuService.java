package com.selfstudy.foodapp.menu.service;

import com.selfstudy.foodapp.menu.dto.MenuDto;
import com.selfstudy.foodapp.response.Response;

import java.util.List;

public interface MenuService {

    Response<MenuDto> createMenu(MenuDto menuDto);
    Response<MenuDto> updateMenu(MenuDto menuDto);
    Response<MenuDto> getMenuById(Long id);
    Response<?>deleteMenu(Long id);
    Response<List<MenuDto>> getMenus(Long categoryId, String search);
}
