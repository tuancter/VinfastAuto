package com.group2.VinfastAuto.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cars")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Long carId;

    private String name;
    private java.math.BigDecimal price;
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