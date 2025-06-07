package com.group2.VinfastAuto.mapper;

import com.group2.VinfastAuto.dto.request.CarRequest;
import com.group2.VinfastAuto.dto.response.CarResponse;
import com.group2.VinfastAuto.entity.Car;
import org.springframework.stereotype.Component;

@Component
public class CarMapper {
    public Car toEntity(CarRequest request) {
        if (request == null) return null;
        return Car.builder()
                .name(request.getName())
                .price(request.getPrice())
                .manufacturedYear(request.getManufacturedYear())
                .state(request.getState())
                .mileage(request.getMileage())
                .origin(request.getOrigin())
                .vehicleType(request.getVehicleType())
                .engine(request.getEngine())
                .exteriorColor(request.getExteriorColor())
                .interiorColor(request.getInteriorColor())
                .seats(request.getSeats())
                .doors(request.getDoors())
                .imgLink(request.getImgLink())
                .description(request.getDescription())
                .build();
    }

    public void updateEntity(Car car, CarRequest request) {
        if (request == null || car == null) return;
        car.setName(request.getName());
        car.setPrice(request.getPrice());
        car.setManufacturedYear(request.getManufacturedYear());
        car.setState(request.getState());
        car.setMileage(request.getMileage());
        car.setOrigin(request.getOrigin());
        car.setVehicleType(request.getVehicleType());
        car.setEngine(request.getEngine());
        car.setExteriorColor(request.getExteriorColor());
        car.setInteriorColor(request.getInteriorColor());
        car.setSeats(request.getSeats());
        car.setDoors(request.getDoors());
        car.setImgLink(request.getImgLink());
        car.setDescription(request.getDescription());
    }

    public CarResponse toResponse(Car car) {
        if (car == null) return null;
        CarResponse response = new CarResponse();
        response.setCarId(car.getCarId());
        response.setName(car.getName());
        response.setPrice(car.getPrice());
        response.setManufacturedYear(car.getManufacturedYear());
        response.setState(car.getState());
        response.setMileage(car.getMileage());
        response.setOrigin(car.getOrigin());
        response.setVehicleType(car.getVehicleType());
        response.setEngine(car.getEngine());
        response.setExteriorColor(car.getExteriorColor());
        response.setInteriorColor(car.getInteriorColor());
        response.setSeats(car.getSeats());
        response.setDoors(car.getDoors());
        response.setImgLink(car.getImgLink());
        response.setDescription(car.getDescription());
        return response;
    }
} 