package com.group2.VinfastAuto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private String id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String position;
    private String mobilephone;
    private String email;
    private LocalDate createdDate;
    private Set<Role> roles;
}
