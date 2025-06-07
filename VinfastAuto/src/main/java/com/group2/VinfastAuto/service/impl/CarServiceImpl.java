package com.group2.VinfastAuto.service.impl;

import com.group2.VinfastAuto.dto.request.CarRequest;
import com.group2.VinfastAuto.dto.response.CarResponse;
import com.group2.VinfastAuto.dto.response.StatisticResponse;
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

    @Override
    public List<StatisticResponse> getCarCountByPriceRange() {
        List<Car> cars = carRepository.findAll();
        int[] ranges = {0, 100_000_000, 200_000_000, 300_000_000, 400_000_000, 500_000_000, 600_000_000, 700_000_000, 800_000_000, 900_000_000, 1_000_000_000};
        String[] labels = {"0 - 100tr", "100tr - 200tr", "200tr - 300tr", "300tr - 400tr", "400tr - 500tr", "500tr - 600tr", "600tr - 700tr", "700tr - 800tr", "800tr - 900tr", "900tr - 1tỷ", "> 1tỷ"};
        int[] counts = new int[labels.length];
        for (Car car : cars) {
            if (car.getPrice() == null) continue;
            long price = car.getPrice().longValue();
            boolean found = false;
            for (int i = 0; i < ranges.length - 1; i++) {
                if (price >= ranges[i] && price < ranges[i+1]) {
                    counts[i]++;
                    found = true;
                    break;
                }
            }
            if (!found && price >= ranges[ranges.length-1]) {
                counts[labels.length-1]++;
            }
        }
        List<StatisticResponse> result = new java.util.ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            result.add(new StatisticResponse(labels[i], counts[i]));
        }
        return result;
    }
} 