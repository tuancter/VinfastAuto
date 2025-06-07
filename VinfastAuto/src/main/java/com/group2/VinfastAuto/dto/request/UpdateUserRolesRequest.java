package com.group2.VinfastAuto.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdateUserRolesRequest {
    @NotEmpty(message = "Role IDs must not be empty")
    private Set<Long> roleIds;
}
