package com.group2.VinfastAuto.configuration;

import com.group2.VinfastAuto.entity.Role;
import com.group2.VinfastAuto.entity.User;
import com.group2.VinfastAuto.repository.InvalidatedTokenRepository;
import com.group2.VinfastAuto.repository.RoleDAO;
import com.group2.VinfastAuto.repository.UserDAO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j // Import logger
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    UserDAO userDAO;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @Bean
    public ApplicationRunner applicationRunner(RoleDAO roleDAO) {
        return args -> {

          // Tao admin mac dinh neu trong db chua co (Dam bao da co role ADMIN trong db)
          if (userDAO.findByUsername("admin").isEmpty()) {
              Set<Role> roles = new HashSet<>();
              roles.add(roleDAO.findById(1L)
                      .orElseThrow(() -> new RuntimeException("ADMIN role must be existed in DB!"))
              );

              User user = User.builder()
                      .id(UUID.randomUUID().toString())
                      .username("admin")
                      .password(passwordEncoder.encode("admin"))
                      .firstName("")
                      .lastName("")
                      .birthday(LocalDate.now())
                      .position("")
                      .mobilephone("")
                      .email("")
                      .createdDate(LocalDate.now())
                      .roles(roles)
                      .build();

              userDAO.add(user);
              userDAO.addRolesToUser(user.getId(), roles);

              log.warn("Admin user has been created with default password: admin, please change it!");
          }

          // Don dep InvalidatedToken
          invalidatedTokenRepository.findAll().forEach(invalidatedToken -> {
              if (invalidatedToken.getExpiryTime().before(new Date())) {
                  invalidatedTokenRepository.deleteById(invalidatedToken.getId());
              }
          });

        };
    }

}
