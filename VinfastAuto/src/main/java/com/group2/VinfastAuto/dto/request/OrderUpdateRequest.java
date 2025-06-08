package com.group2.VinfastAuto.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class OrderUpdateRequest {

    private Long carId;
    private LocalDate orderDate;
    private int quantity;
    private double price;
    private String status;
    private String placeOfPurchase;
}