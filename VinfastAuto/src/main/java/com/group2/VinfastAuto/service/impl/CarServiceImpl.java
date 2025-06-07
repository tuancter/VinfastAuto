package com.group2.VinfastAuto.service.impl;

import com.group2.VinfastAuto.dto.request.CarRequest;
import com.group2.VinfastAuto.dto.response.CarResponse;
import com.group2.VinfastAuto.entity.Car;
import com.group2.VinfastAuto.mapper.CarMapper;
import com.group2.VinfastAuto.repository.CarRepository;
import com.group2.VinfastAuto.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarResponse createCar(CarRequest request) {
        Car car = carMapper.toEntity(request);
        return carMapper.toResponse(carRepository.save(car));
    }

    @Override
    public CarResponse updateCar(Long carId, CarRequest request) {
        Car car = carRepository.findById(carId).orElseThrow(() -> new RuntimeException("Car not found"));
        carMapper.updateEntity(car, request);
        return carMapper.toResponse(carRepository.save(car));
    }

    @Override
    public void deleteCar(Long carId) {
        carRepository.deleteById(carId);
    }

    @Override
    public CarResponse getCarById(Long carId) {
        Car car = carRepository.findById(carId).orElseThrow(() -> new RuntimeException("Car not found"));
        return carMapper.toResponse(car);
    }

    @Override
    public List<CarResponse> getAllCars() {
        return carRepository.findAll().stream().map(carMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public Page<CarResponse> searchCars(String keyword, Pageable pageable) {
        Page<Car> page = carRepository.findAll(pageable); // Có thể custom lại search theo keyword
        List<CarResponse> responses = page.getContent().stream().map(carMapper::toResponse).collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }
} 