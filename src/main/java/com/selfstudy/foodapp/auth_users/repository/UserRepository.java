package com.selfstudy.foodapp.auth_users.repository;

import com.selfstudy.foodapp.auth_users.entity.User;
import com.selfstudy.foodapp.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
