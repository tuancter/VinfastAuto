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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public Page<CarResponse> searchCars(String keyword, String sortBy, String direction, Pageable pageable) {
        // Xác định trường sort hợp lệ
        final String sortField = "price".equalsIgnoreCase(sortBy) ? "price" :
                ("manufacturedYear".equalsIgnoreCase(sortBy) ? "manufacturedYear" : "name");
        // Xác định chiều sort
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Car> page;
        if (keyword == null || keyword.isBlank()) {
            page = carRepository.findAll(sortedPageable);
        } else {
            List<Car> filtered = carRepository.findAll().stream()
                .filter(car -> car.getName() != null && car.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
            filtered.sort((a, b) -> {
                int cmp = 0;
                if ("name".equals(sortField)) {
                    cmp = a.getName().compareToIgnoreCase(b.getName());
                } else if ("price".equals(sortField)) {
                    cmp = a.getPrice() == null ? 1 : b.getPrice() == null ? -1 : a.getPrice().compareTo(b.getPrice());
                } else if ("manufacturedYear".equals(sortField)) {
                    cmp = a.getManufacturedYear() == null ? 1 : b.getManufacturedYear() == null ? -1 : a.getManufacturedYear().compareTo(b.getManufacturedYear());
                }
                return "desc".equalsIgnoreCase(direction) ? -cmp : cmp;
            });
            int start = (int) sortedPageable.getOffset();
            int end = Math.min(start + sortedPageable.getPageSize(), filtered.size());
            List<Car> pageContent = start > end ? List.of() : filtered.subList(start, end);
            page = new PageImpl<>(pageContent, sortedPageable, filtered.size());
        }
        List<CarResponse> responses = page.getContent().stream().map(carMapper::toResponse).collect(Collectors.toList());
        return new PageImpl<>(responses, sortedPageable, page.getTotalElements());
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