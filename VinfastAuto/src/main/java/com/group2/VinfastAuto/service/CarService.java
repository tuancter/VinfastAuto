package com.group2.VinfastAuto.service;

import com.group2.VinfastAuto.dto.request.CarRequest;
import com.group2.VinfastAuto.dto.response.CarResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CarService {
    CarResponse createCar(CarRequest request);
    CarResponse updateCar(Long carId, CarRequest request);
    void deleteCar(Long carId);
    CarResponse getCarById(Long carId);
    List<CarResponse> getAllCars();
    Page<CarResponse> searchCars(String keyword, Pageable pageable);
} 