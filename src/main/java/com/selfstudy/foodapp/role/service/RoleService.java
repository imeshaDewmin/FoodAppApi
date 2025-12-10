package com.selfstudy.foodapp.role.service;

import com.selfstudy.foodapp.response.Response;
import com.selfstudy.foodapp.role.dto.RoleDto;

import java.util.List;

public interface RoleService {

    Response<RoleDto>createRole(RoleDto roleDto);
    Response<RoleDto>updateRole(RoleDto roleDto);
    Response <List<RoleDto>> getAllRoles();
    Response<?> deleteRole(Long id);
}
