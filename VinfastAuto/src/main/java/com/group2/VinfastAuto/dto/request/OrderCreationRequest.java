package com.group2.VinfastAuto.dto.request;

import lombok.Data;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

@Data
public class OrderCreationRequest {
    @NotNull(message = "userId không được để trống")
    private Long userId;
    @NotNull(message = "carId không được để trống")
    private Long carId;
    @NotNull(message = "orderDate không được để trống")
    private java.time.LocalDate orderDate;
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;
    @Min(value = 1, message = "Giá phải lớn hơn 0")
    private double price;
    @NotBlank(message = "Trạng thái không được để trống")
    private String status;
    @NotBlank(message = "Nơi mua không được để trống")
    private String placeOfPurchase;
}