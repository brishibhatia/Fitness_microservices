package com.fitness.userservice.repository;

import com.fitness.userservice.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface userRepository extends JpaRepository<User,String > {

    boolean existsByEmail(String email);

    User findByEmail(@NotBlank(message = "Email is Required") @Email(message = "Invalid Format") String email);
}
