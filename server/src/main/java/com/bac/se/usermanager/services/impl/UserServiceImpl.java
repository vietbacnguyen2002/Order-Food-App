package com.bac.se.usermanager.services.impl;

import com.bac.se.usermanager.dto.response.UserPageResponse;
import com.bac.se.usermanager.dto.response.UserResponse;
import com.bac.se.usermanager.exceptions.UnauthorizedException;
import com.bac.se.usermanager.exceptions.UserNotFoundException;
import com.bac.se.usermanager.models.User;
import com.bac.se.usermanager.repositories.UserRepository;
import com.bac.se.usermanager.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserResponse userResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(),
                user.getPhone(), user.getFirstName(), user.getLastName());
    }

    @Override
    public UserPageResponse getUsers(Integer pageNumber, Integer pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            Page<User> userPage = userRepository.findAll(pageable);
            List<User> users = userPage.getContent();
            List<UserResponse> userResponses = users.stream().map(this::userResponse).toList();
            return new UserPageResponse(
                    userResponses,
                    pageNumber,
                    pageSize,
                    userPage.getTotalElements(),
                    userPage.getTotalPages(),
                    userPage.isLast()
            );
        }catch (Exception e){
            log.error(e.getMessage());
            throw new UnauthorizedException("Unauthorized");
        }
    }

    @Override
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Not found user with id :" + id));
        return new UserResponse(user.getId(), user.getEmail(),
                user.getPhone(), user.getFirstName(), user.getLastName());

    }


    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean updateUser(User user) {
        log.info("Id user is {}", user.getId());
        User userFind = userRepository.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Not found user with id : " + user.getId()));
        userFind.setFirstName(user.getFirstName());
        userFind.setLastName(user.getLastName());
        userFind.setPhone(user.getPhone());
        userRepository.save(userFind);
        return true;
    }
}
