package com.group2.VinfastAuto.controller;

import com.group2.VinfastAuto.dto.request.UpdateUserRolesRequest;
import com.group2.VinfastAuto.dto.request.UserCreationRequest;
import com.group2.VinfastAuto.dto.request.UserUpdateRequest;
import com.group2.VinfastAuto.dto.response.*;
import com.group2.VinfastAuto.enums.StatusCode;
import com.group2.VinfastAuto.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {

    UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserCreationRequest request) {
        UserResponse userResponse = userService.create(request);
        return ApiResponse.<UserResponse>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(userResponse)
                .build();

    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAll() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("Username: {}", authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        List<UserResponse> userList = userService.getAll();
        return ApiResponse.<List<UserResponse>>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(userList)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(@PathVariable("id") String id) {
        UserResponse userResponse = userService.getById(id);
        return ApiResponse.<UserResponse>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(userResponse)
                .build();
    }

    @GetMapping("/myInfo")
    public ApiResponse<UserResponse> getCurrentInfo() {
        UserResponse userResponse = userService.getCurrentInfo();
        return ApiResponse.<UserResponse>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(userResponse)
                .build();
    }

    @GetMapping("/myRole")
    public ApiResponse<RoleResponse> getCurrentRole() {
        RoleResponse roleResponse = userService.getCurrentRole();
        return ApiResponse.<RoleResponse>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(roleResponse)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateById(@PathVariable("id") String id, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse userResponse = userService.updateById(id, request);
        return ApiResponse.<UserResponse>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(userResponse)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteById(@PathVariable("id") String id) {
        UserResponse userResponse = userService.getById(id);
        userService.deleteById(id);
        return ApiResponse.<UserResponse>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .message("User has been deleted!")
                .data(userResponse)
                .build();
    }

    // Pagination
    @GetMapping("/search")
    public ApiResponse<PageResponse<UserResponse>> searchUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "first_name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        PageResponse<UserResponse> result = userService.searchUsers(keyword, sortBy, direction, page, size);
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(result)
                .build();
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<Void> updateRoles(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateUserRolesRequest request) {

        userService.updateRolesOfUser(id, request.getRoleIds());

        return ApiResponse.<Void>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .message("Roles updated successfully")
                .build();
    }

    @GetMapping("/{id}/roles")
    public ApiResponse<RoleResponse> getUserRoles(@PathVariable("id") String id) {
        RoleResponse roleResponse = userService.getRolesOfUser(id);
        return ApiResponse.<RoleResponse>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(roleResponse)
                .build();
    }

    // Thống kê số lượng user theo năm tạo
    @GetMapping("/statistics/by-year")
    public ApiResponse<List<StatisticResponse>> getUserCountByYear() {
        List<StatisticResponse> stats = userService.getUserCountByYear();
        return ApiResponse.<List<StatisticResponse>>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(stats)
                .build();
    }

    // Thống kê số lượng user theo vị trí
    @GetMapping("/statistics/by-position")
    public ApiResponse<List<StatisticResponse>> getUserCountByPosition() {
        List<StatisticResponse> stats = userService.getUserCountByPosition();
        return ApiResponse.<List<StatisticResponse>>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(stats)
                .build();
    }

    // Thống kê số lượng user theo nhóm tuổi
    @GetMapping("/statistics/by-age-group")
    public ApiResponse<List<StatisticResponse>> getUserCountByAgeGroup() {
        List<StatisticResponse> stats = userService.getUserCountByAgeGroup();
        return ApiResponse.<List<StatisticResponse>>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(stats)
                .build();
    }

}

