package com.group2.VinfastAuto.service;

import com.group2.VinfastAuto.dto.request.OrderCreationRequest;
import com.group2.VinfastAuto.dto.request.OrderUpdateRequest;
import com.group2.VinfastAuto.dto.response.OrderResponse;
import com.group2.VinfastAuto.dto.response.PageResponse;
import com.group2.VinfastAuto.dto.response.StatisticResponse;
import com.group2.VinfastAuto.entity.Car;
import com.group2.VinfastAuto.entity.Order;
import com.group2.VinfastAuto.entity.User;
import com.group2.VinfastAuto.enums.OrderStatus;
import com.group2.VinfastAuto.exception.ResourceNotFoundException;
import com.group2.VinfastAuto.mapper.OrderMapper;
import com.group2.VinfastAuto.repository.CarRepository;
import com.group2.VinfastAuto.repository.OrderRepository;
import com.group2.VinfastAuto.repository.UserDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Double.NaN;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserDAO userDAO;
    private final CarRepository carRepository;    /**
     * Creates a new order in the system.
     * The price is obtained from the associated car entity.
     */
    public OrderResponse create(OrderCreationRequest request) {
        Order order = new Order();

        User user = userDAO.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found"));

        order.setUser(user);
        order.setCar(car);
        order.setOrderDate(request.getOrderDate() != null 
            ? request.getOrderDate().atStartOfDay() 
            : LocalDateTime.now());
        order.setStatus(OrderStatus.valueOf(request.getStatus()));
        order.setCreatedAt(LocalDateTime.now());

        return orderMapper.orderToOrderResponse(orderRepository.save(order));
    }

    public OrderResponse update(Long id, OrderUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id " + id));

        // Không cho thay đổi customer
        // Nếu bạn muốn bảo vệ nghiêm ngặt hơn nữa, hãy bỏ hẳn customerId khỏi DTO

        // Có thể cho thay đổi Car
        if (request.getCarId() != null) {
            Car car = carRepository.findById(request.getCarId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Car not found with id " + request.getCarId()));
            order.setCar(car);
        }        // Cập nhật các trường còn lại
        if (request.getOrderDate() != null) {
            order.setOrderDate(request.getOrderDate().atStartOfDay());
        }

        if (request.getStatus() != null) {
            try {
                order.setStatus(OrderStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Invalid order status: " + request.getStatus());
            }
        }

        Order updated = orderRepository.save(order);
        return mapToDto(updated);
    }
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with id " + id);
        }
        orderRepository.deleteById(id);
    }

    public List<OrderResponse> getAll() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public OrderResponse getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id " + id));
        return mapToDto(order);
    }

    public List<Object[]> countTotalOrdersByStatus() {
        return orderRepository.countTotalOrdersByStatus();
    }
    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
    public Page<Order> searchByUserLastName(String keyword, Pageable pageable) {
        return orderRepository.findByUser_LastNameContainingIgnoreCase(keyword, pageable);
    }
    public Page<Order> searchOrders(Pageable pageable, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return orderRepository.findAll(pageable);
        }
        return orderRepository.findByUser_LastNameContainingIgnoreCase(keyword, pageable);
    }    // Ánh xạ thủ công sang DTO
    private OrderResponse mapToDto(Order o) {
        OrderResponse dto = new OrderResponse();
        dto.setId(o.getId());

        if (o.getUser() != null) {
            dto.setUserId(o.getUser().getId());
            String fullName = (o.getUser().getLastName() != null ? o.getUser().getLastName() : "") +
                              (o.getUser().getFirstName() != null ? (" " + o.getUser().getFirstName()) : "");
            dto.setCustomerName(fullName.trim());
            dto.setPhoneNumber(o.getUser().getMobilephone() != null ? o.getUser().getMobilephone() : "");
            dto.setEmail(o.getUser().getEmail() != null ? o.getUser().getEmail() : "");
            dto.setAddress(o.getUser().getPosition() != null ? o.getUser().getPosition() : "");
        } else {
            dto.setUserId(null);
            dto.setCustomerName("");
            dto.setPhoneNumber("");
            dto.setEmail("");
            dto.setAddress("");
        }        if (o.getCar() != null) {
            dto.setCarId(o.getCar().getCarId());
            dto.setCarModel(o.getCar().getName() != null
                    ? o.getCar().getName()
                    : "");
            dto.setCarPrice(o.getCar().getPrice()); // Get price from the car entity
        } else {
            dto.setCarId(null);
            dto.setCarModel("");
            dto.setCarPrice(null);
        }
        
        dto.setOrderDate(o.getOrderDate() != null
                ? o.getOrderDate().toLocalDate()
                : null);
        
        dto.setStatus(o.getStatus() != null ? o.getStatus().name() : null);
        dto.setCreatedAt(o.getCreatedAt());

        return dto;
    }

    public OrderResponse toResponse(Order order) {
        return orderMapper.orderToOrderResponse(order);
    }
    // Hàm search + phân trang
    public PageResponse<OrderResponse> searchOrders(String keyword, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> ordersPage;
        if (keyword == null || keyword.trim().isEmpty()) {
            ordersPage = orderRepository.findAll(pageable);
        } else {
            ordersPage = orderRepository.findByUser_LastNameContainingIgnoreCase(keyword, pageable);
        }

        List<OrderResponse> dtoList = ordersPage.getContent()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PageResponse<>(
                dtoList,
                ordersPage.getNumber(),
                ordersPage.getSize(),
                ordersPage.getTotalElements(),
                ordersPage.getTotalPages()
        );
    }

    // Thống kê đơn hàng theo trạng thái
    public List<StatisticResponse> getOrderCountByStatus() {
        List<Object[]> raw = orderRepository.countTotalOrdersByStatus();
        List<StatisticResponse> result = new java.util.ArrayList<>();
        for (Object[] o : raw) {
            result.add(new StatisticResponse(o[0].toString(), ((Number) o[1]).intValue()));
        }
        return result;
    }

    // Thống kê đơn hàng theo khoảng giá trị
    public List<StatisticResponse> getOrderCountByPriceRange() {
        List<Order> orders = orderRepository.findAll();
        double[] ranges = {0, 100_000_000, 200_000_000, 300_000_000, 400_000_000, 500_000_000, 600_000_000, 700_000_000, 800_000_000, 900_000_000, 1_000_000_000};
        String[] labels = {"0 - 100tr", "100tr - 200tr", "200tr - 300tr", "300tr - 400tr", "400tr - 500tr", "500tr - 600tr", "600tr - 700tr", "700tr - 800tr", "800tr - 900tr", "900tr - 1tỷ", "> 1tỷ"};
        int[] counts = new int[labels.length];
        for (Order order : orders) {
            double price = order.getCar() != null && order.getCar().getPrice() != null
                    ? order.getCar().getPrice().doubleValue()
                    : NaN; // Sử dụng NaN nếu giá không hợp lệ
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
