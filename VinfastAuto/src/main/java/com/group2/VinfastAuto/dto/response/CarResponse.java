package com.group2.VinfastAuto.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CarResponse {
    private Long carId;
    private String name;
    private BigDecimal price;
    private Short manufacturedYear;
    private String state;
    private Integer mileage;
    private String origin;
    private String vehicleType;
    private String engine;
    private String exteriorColor;
    private String interiorColor;
    private Short seats;
    private Short doors;
    private String imgLink;
    private String description;
} 