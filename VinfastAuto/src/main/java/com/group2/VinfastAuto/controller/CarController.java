package com.group2.VinfastAuto.controller;

import com.group2.VinfastAuto.dto.request.CarRequest;
import com.group2.VinfastAuto.dto.response.ApiResponse;
import com.group2.VinfastAuto.dto.response.CarResponse;
import com.group2.VinfastAuto.dto.response.PageResponse;
import com.group2.VinfastAuto.dto.response.StatisticResponse;
import com.group2.VinfastAuto.enums.StatusCode;
import com.group2.VinfastAuto.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class CarController {
    CarService carService;

    @PostMapping
    public ApiResponse<CarResponse> create(@RequestBody CarRequest request) {
        CarResponse carResponse = carService.createCar(request);
        return ApiResponse.<CarResponse>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(carResponse)
                .build();
    }

    @GetMapping
    public ApiResponse<List<CarResponse>> getAll() {
        List<CarResponse> cars = carService.getAllCars();
        return ApiResponse.<List<CarResponse>>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(cars)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CarResponse> getById(@PathVariable("id") Long id) {
        CarResponse carResponse = carService.getCarById(id);
        return ApiResponse.<CarResponse>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(carResponse)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CarResponse> updateById(@PathVariable("id") Long id, @RequestBody CarRequest request) {
        CarResponse carResponse = carService.updateCar(id, request);
        return ApiResponse.<CarResponse>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(carResponse)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable("id") Long id) {
        carService.deleteCar(id);
        return ApiResponse.<Void>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .message("Car has been deleted!")
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<CarResponse>> searchCars(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CarResponse> result = carService.searchCars(keyword, pageable);
        PageResponse<CarResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(result.getContent());
        pageResponse.setTotalElements(result.getTotalElements());
        pageResponse.setTotalPages(result.getTotalPages());
        pageResponse.setPage(result.getNumber());
        pageResponse.setSize(result.getSize());
        return ApiResponse.<PageResponse<CarResponse>>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(pageResponse)
                .build();
    }

    @GetMapping("/statistics/by-price-range")
    public ApiResponse<List<StatisticResponse>> getCarCountByPriceRange() {
        List<StatisticResponse> stats = carService.getCarCountByPriceRange();
        return ApiResponse.<List<StatisticResponse>>builder()
                .statusCode(StatusCode.SUCCESS.getCode())
                .data(stats)
                .build();
    }
} 