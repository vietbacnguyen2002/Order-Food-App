package com.bac.se.usermanager.controllers;

import com.bac.se.usermanager.dto.response.UserPageResponse;
import com.bac.se.usermanager.dto.response.UserResponse;
import com.bac.se.usermanager.exceptions.UnauthorizedException;
import com.bac.se.usermanager.models.User;
import com.bac.se.usermanager.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserPageResponse getUsers(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        try {
            return userService.getUsers(pageNumber, pageSize);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new UnauthorizedException("Unauthorized");
        }
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    public UserResponse getUserById(@PathVariable("id") Long id) {
        return userService.getUser(id);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public String updateUser(@RequestBody User user) {
        return userService.updateUser(user) ? "Update Success" : "Update Failed";
    }

}
