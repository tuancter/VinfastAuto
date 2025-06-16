package com.group2.VinfastAuto.dto.request;

import lombok.Data;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

/**
 * DTO for creating a new order.
 * The price is obtained from the car entity, so it doesn't need to be provided.
 */
@Data
public class OrderCreationRequest {
    @NotBlank(message = "userId không được để trống")
    private String userId;
    
    @NotNull(message = "carId không được để trống")
    private Long carId;
    
    @NotNull(message = "orderDate không được để trống")
    private LocalDate orderDate;
    
    @NotBlank(message = "Trạng thái không được để trống")
    private String status;
}