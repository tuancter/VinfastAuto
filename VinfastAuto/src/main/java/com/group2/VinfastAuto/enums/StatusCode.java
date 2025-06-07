package com.group2.VinfastAuto.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum StatusCode {
    UNKNOWERROR(9999, "Unknow error!", HttpStatus.INTERNAL_SERVER_ERROR),

    AUTHENTICATED(1000, "Authenticated", HttpStatus.OK),
    UNAUTHENTICATED(1001, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "Unauthozied!", HttpStatus.FORBIDDEN),

    SUCCESS(2000, "Success!", HttpStatus.CREATED),
    USER_NOT_FOUND(2001, "User not found!", HttpStatus.NOT_FOUND),
    USER_EXISTED(2002, "User existed", HttpStatus.BAD_REQUEST),
    USER_HAS_NO_ROLE(2003, "User has no role!", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(2004, "Username must be at least {min} characters!", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(2005, "Password must be at least {min} characters!", HttpStatus.BAD_REQUEST),
    INVALID_DOB(2006, "You age must be at least {min}", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(2007, "Role not found", HttpStatus.BAD_REQUEST)


    ;

    int code;
    String message;
    HttpStatusCode httpStatusCode;

    StatusCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }


}
