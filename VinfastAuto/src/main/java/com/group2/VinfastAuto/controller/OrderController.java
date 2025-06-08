package com.group2.VinfastAuto.controller;

import com.group2.VinfastAuto.dto.request.OrderCreationRequest;
import com.group2.VinfastAuto.dto.request.OrderUpdateRequest;
import com.group2.VinfastAuto.dto.response.ApiResponse;
import com.group2.VinfastAuto.dto.response.OrderResponse;
import com.group2.VinfastAuto.dto.response.PageResponse;
import com.group2.VinfastAuto.dto.response.StatisticResponse;
import com.group2.VinfastAuto.entity.Car;
import com.group2.VinfastAuto.entity.Order;

import com.group2.VinfastAuto.enums.OrderStatus;
import com.group2.VinfastAuto.repository.CarRepository;
import com.group2.VinfastAuto.repository.OrderRepository;
import com.group2.VinfastAuto.service.OrderService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    @Autowired
    private OrderRepository OrderRepository;
    @Autowired
    private CarRepository CarRepository;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody OrderCreationRequest request) {
        return ResponseEntity.ok(orderService.create(request));
    }


    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id,
                                                @RequestBody OrderUpdateRequest request) {
        return ResponseEntity.ok(orderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }



    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getAll(Pageable pageable) {
        Page<Order> page = orderService.findAll(pageable);
        List<OrderResponse> dtoList = page.stream()
                .map(orderService::toResponse)
                .collect(Collectors.toList());

        PageResponse<OrderResponse> resp = PageResponse.<OrderResponse>builder()
                .content(dtoList)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return ResponseEntity.ok(resp);
    }



    //    GET /orders/search?keyword=xxx&page=0&size=10&sortBy=orderDate&direction=desc

    @GetMapping("/search")
    public ResponseEntity<PageResponse<OrderResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(orderService.searchOrders(keyword, page, size, sortBy, direction));
    }

    //    GET /orders/statistics/by-status
    @GetMapping("/statistics/by-status")
    public ApiResponse<List<StatisticResponse>> statsByStatus() {
        List<StatisticResponse> stats = orderService.getOrderCountByStatus();
        return ApiResponse.<List<StatisticResponse>>builder()
                .statusCode(2000)
                .data(stats)
                .build();
    }

    @GetMapping("/statistics/by-price-range")
    public ApiResponse<List<StatisticResponse>> getOrderCountByPriceRange() {
        List<StatisticResponse> stats = orderService.getOrderCountByPriceRange();
        return ApiResponse.<List<StatisticResponse>>builder()
                .statusCode(2000)
                .data(stats)
                .build();
    }
}
