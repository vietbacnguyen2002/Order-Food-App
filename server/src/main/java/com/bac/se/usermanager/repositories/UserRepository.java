package com.bac.se.usermanager.repositories;

import com.bac.se.usermanager.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByPhone(String phone);

    @Transactional
    @Modifying
    @Query("update User u set u.isValid = true where u.email = :email")
    void updateValidEmail(@Param("email")String email);

    @Transactional
    @Modifying
    @Query("update User u set u.password = ?2 where u.email = ?1")
    void updatePassword(String email, String password);
}
