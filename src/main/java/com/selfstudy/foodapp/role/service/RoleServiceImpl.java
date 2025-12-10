package com.selfstudy.foodapp.role.service;

import com.selfstudy.foodapp.exceptions.BadRequestException;
import com.selfstudy.foodapp.exceptions.NotFoundException;
import com.selfstudy.foodapp.response.Response;
import com.selfstudy.foodapp.role.dto.RoleDto;
import com.selfstudy.foodapp.role.entity.Role;
import com.selfstudy.foodapp.role.repository.RoleRepository;
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
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Response<RoleDto> createRole(RoleDto roleDto) {
        Role role = modelMapper.map(roleDto,Role.class);

        Role savedRole = roleRepository.save(role);

        return Response.<RoleDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role created successfully")
                .data(modelMapper.map(savedRole,RoleDto.class))
                .build();
    }

    @Override
    public Response<RoleDto> updateRole(RoleDto roleDto) {
        Role existingRole = roleRepository.findById(roleDto.getId())
                .orElseThrow(()-> new NotFoundException("Role not found"));

        if (roleRepository.findByName(roleDto.getName()).isPresent()){
            throw new BadRequestException("Role with name already exists");
        }

        existingRole.setName(roleDto.getName());

        Role updatedRole = roleRepository.save(existingRole);
        return Response.<RoleDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role updated successfully")
                .data(modelMapper.map(updatedRole,RoleDto.class))
                .build();
    }

    @Override
    public Response<List<RoleDto>> getAllRoles() {
        List <Role> roles = roleRepository.findAll();

        List <RoleDto> roleDto = roles.stream().map(role -> modelMapper.map(role,RoleDto.class)).toList();

        return Response.<List<RoleDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Roles retrieved successfully")
                .data(roleDto)
                .build();
    }

    @Override
    public Response<?>deleteRole(Long id){
        roleRepository.deleteById(id);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role deleted successfully")
                .build();
    }
}
