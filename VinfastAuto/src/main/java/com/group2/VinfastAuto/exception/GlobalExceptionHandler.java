package com.group2.VinfastAuto.exception;

import com.group2.VinfastAuto.dto.response.ApiResponse;
import com.group2.VinfastAuto.enums.StatusCode;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    public static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
        HttpStatus status;

        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        } else if (ex instanceof org.springframework.security.core.AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ApiResponse apiResponse = ApiResponse.builder()
                .statusCode(status.value())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(status).body(apiResponse);
    }


    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        StatusCode statusCode = exception.getStatusCode();
        ApiResponse apiResponse = ApiResponse.builder()
                .statusCode(statusCode.getCode())
                .message(exception.getMessage())
                .build();

        return ResponseEntity
                .status(statusCode.getHttpStatusCode())
                .body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();
        StatusCode statusCode = StatusCode.valueOf(enumKey);

        ConstraintViolation constraintViolation = exception
                .getBindingResult()
                .getAllErrors()
                .getFirst()
                .unwrap(ConstraintViolation.class);

        Map<String, Object> atributes = constraintViolation.getConstraintDescriptor().getAttributes();

        ApiResponse apiResponse = ApiResponse.builder()
                .statusCode(statusCode.getCode())
                .message(mapAttribute(statusCode.getMessage(), atributes))
                .build();

        return ResponseEntity
                .status(statusCode.getHttpStatusCode())
                .body(apiResponse);
    }


    // Helper method
    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));

        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
