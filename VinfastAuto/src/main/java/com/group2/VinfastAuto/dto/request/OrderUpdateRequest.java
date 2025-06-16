package com.group2.VinfastAuto.dto.request;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for updating an existing order.
 * User cannot be changed after creation.
 * Price is derived from the car entity, so it's not included here.
 */
@Data
public class OrderUpdateRequest {
    // User ID is not included since it shouldn't be changed after creation
    private Long carId;
    private LocalDate orderDate;
    private String status;
}