package com.group2.VinfastAuto.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for orders.
 * Contains both order data and some related data from the user and car entities.
 */
@Data
public class OrderResponse {
    // Order data
    private Long id;
    private String userId;
    private Long carId;
    private LocalDate orderDate;
    private String status;
    private LocalDateTime createdAt;
    
    // User related data
    private String customerName;
    private String phoneNumber;
    private String email;
    private String address;
    
    // Car related data
    private String carModel;
    private BigDecimal carPrice; // Price from the car entity
}
