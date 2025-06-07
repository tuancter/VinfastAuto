package com.group2.VinfastAuto.controller;

import com.group2.VinfastAuto.dto.request.AuthenticationRequest;
import com.group2.VinfastAuto.dto.request.IntrospectRequest;
import com.group2.VinfastAuto.dto.request.LogoutRequest;
import com.group2.VinfastAuto.dto.request.RefreshRequest;
import com.group2.VinfastAuto.dto.response.ApiResponse;
import com.group2.VinfastAuto.dto.response.AuthenticationResponse;
import com.group2.VinfastAuto.dto.response.IntrospectResponse;
import com.group2.VinfastAuto.enums.StatusCode;
import com.group2.VinfastAuto.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse authenticationResponse =  authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .statusCode(StatusCode.AUTHENTICATED.getCode())
                .data(authenticationResponse)
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) {
        IntrospectResponse introspectResponse =  authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .statusCode(StatusCode.AUTHENTICATED.getCode())
                .data(introspectResponse)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .statusCode(StatusCode.AUTHENTICATED.getCode())
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        AuthenticationResponse authenticationResponse = authenticationService.refresh(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .statusCode(StatusCode.AUTHENTICATED.getCode())
                .data(authenticationResponse)
                .build();
    }

    

}
