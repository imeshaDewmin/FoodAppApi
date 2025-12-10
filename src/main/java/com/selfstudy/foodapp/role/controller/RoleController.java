package com.selfstudy.foodapp.role.controller;

import com.selfstudy.foodapp.response.Response;
import com.selfstudy.foodapp.role.dto.RoleDto;
import com.selfstudy.foodapp.role.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/roles")
@PreAuthorize("hasAuthority('ADMIN')")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping("/create")
    ResponseEntity<Response<RoleDto>> createRole(@RequestBody @Valid RoleDto roleDto){
        return ResponseEntity.ok(roleService.createRole(roleDto));
    }

    @PutMapping("/update")
    ResponseEntity<Response<RoleDto>> updateRole(@RequestBody @Valid RoleDto roleDto) {
        return ResponseEntity.ok(roleService.updateRole(roleDto));
    }

    @GetMapping
    ResponseEntity<Response<List<RoleDto>>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Response<?>> deleteRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.deleteRole(id));
    }

}