package com.group2.VinfastAuto.mapper;

import com.group2.VinfastAuto.dto.request.UserCreationRequest;
import com.group2.VinfastAuto.dto.request.UserUpdateRequest;
import com.group2.VinfastAuto.dto.response.UserResponse;
import com.group2.VinfastAuto.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    public User userCreationRequestToUser(UserCreationRequest request);

    public UserResponse userToUserResponse(User user);

    public void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
