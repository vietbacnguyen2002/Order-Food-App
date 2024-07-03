package com.bac.se.usermanager.services;

import com.bac.se.usermanager.dto.response.UserPageResponse;
import com.bac.se.usermanager.dto.response.UserResponse;
import com.bac.se.usermanager.models.User;


public interface UserService {
    UserPageResponse getUsers(Integer pageNumber, Integer pageSize);
    UserResponse getUser(Long id);
    void deleteUser(Long id);
    boolean updateUser(User user);

}
