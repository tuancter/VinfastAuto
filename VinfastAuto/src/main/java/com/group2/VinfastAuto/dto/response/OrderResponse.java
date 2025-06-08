package com.group2.VinfastAuto.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class OrderResponse {
    private Long id;
    private String customerName;
    private String phoneNumber;
    private LocalDate orderDate;
    private String carModel;
    private int quantity;
    private double price;
    private String status;
    private String placeOfPurchase;
    private String email;
    private String address;
}
