package com.group2.VinfastAuto.service;

import com.group2.VinfastAuto.dto.request.UserCreationRequest;
import com.group2.VinfastAuto.dto.request.UserUpdateRequest;
import com.group2.VinfastAuto.dto.response.PageResponse;
import com.group2.VinfastAuto.dto.response.RoleResponse;
import com.group2.VinfastAuto.dto.response.StatisticResponse;
import com.group2.VinfastAuto.dto.response.UserResponse;
import com.group2.VinfastAuto.entity.Role;
import com.group2.VinfastAuto.entity.User;
import com.group2.VinfastAuto.enums.StatusCode;
import com.group2.VinfastAuto.exception.AppException;
import com.group2.VinfastAuto.mapper.UserMapper;
import com.group2.VinfastAuto.repository.RoleDAO;
import com.group2.VinfastAuto.repository.UserDAO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserDAO userDAO;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    RoleDAO roleDAO;

    private static final Long DEFAULT_USER_ROLE_ID = 2L;

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAll() {
        List<UserResponse> userResponseList = new ArrayList<>();
        userDAO.findAll().forEach(user ->
                userResponseList.add(userMapper.userToUserResponse(user)));
        return userResponseList;
    }

    @PostAuthorize("returnObject.username == authentication.name || hasRole('ADMIN')")
    public UserResponse getById(String id) {
        User user = userDAO.findById(id)
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));
        return userMapper.userToUserResponse(user);
    }

    public UserResponse create(UserCreationRequest request) {
        if (userDAO.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(StatusCode.USER_EXISTED);
        }

        User user = userMapper.userCreationRequestToUser(request);
        user.setId(UUID.randomUUID().toString());
        user.setCreatedDate(LocalDate.now());

        Set<Role> roles = new HashSet<>();
        roles.add(roleDAO.findById(DEFAULT_USER_ROLE_ID)
                .orElseThrow(() -> new RuntimeException("USER role must exist in DB!")));
        user.setRoles(roles);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userDAO.add(user);
        userDAO.addRolesToUser(user.getId(), roles);

        return userMapper.userToUserResponse(user);
    }

    public UserResponse updateById(String id, UserUpdateRequest request) {
        User user = userDAO.findById(id)
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));
        String currentPassword = user.getPassword();
        userMapper.updateUser(user, request);
        user.setId(id);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        } else {
            user.setPassword(currentPassword);
        }

        return userMapper.userToUserResponse(userDAO.update(user));
    }

    public void deleteById(String id) {
        User user = userDAO.findById(id)
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));
        userDAO.delete(user);
    }

    public UserResponse getCurrentInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDAO.findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));
        return userMapper.userToUserResponse(user);
    }

    public RoleResponse getCurrentRole() {
        RoleResponse roleResponse = new RoleResponse();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDAO.findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));
        roleResponse.setRoles(userDAO.getRolesOfUser(user.getId()));
        return roleResponse;
    }

    // Pagination
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserResponse> searchUsers(String keyword, String sortBy, String direction, int page, int size) {
        if (!List.of("first_name", "created_date").contains(sortBy)) {
            sortBy = "first_name"; // Mặc định
        }

        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            direction = "asc";
        }

        int offset = page * size;

        List<User> users = userDAO.searchUsers(keyword, sortBy, direction, offset, size);
        int total = userDAO.countUsers(keyword);

        List<UserResponse> userResponses = users.stream()
                .map(userMapper::userToUserResponse)
                .toList();

        return PageResponse.<UserResponse>builder()
                .content(userResponses)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / size))
                .build();
    }

    // Update roles
    @PreAuthorize("hasRole('ADMIN')")
    public void updateRolesOfUser(String userId, Set<Long> roleIds) {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));

        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleDAO.findById(roleId)
                    .orElseThrow(() -> new AppException(StatusCode.ROLE_NOT_FOUND));
            roles.add(role);
        }

        userDAO.updateRolesOfUser(userId, roles);
    }

    // Get role
    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse getRolesOfUser(String userId) {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));

        RoleResponse roleResponse = new RoleResponse();
        roleResponse.setRoles(userDAO.getRolesOfUser(user.getId()));
        return roleResponse;
    }

    // Statistic
    public List<StatisticResponse> getUserCountByYear() {
        return userDAO.countUsersByYear();
    }

    public List<StatisticResponse> getUserCountByPosition() {
        return userDAO.countUsersByPosition();
    }

    public List<StatisticResponse> getUserCountByAgeGroup() {
        return userDAO.countUsersByAgeGroup();
    }


}
