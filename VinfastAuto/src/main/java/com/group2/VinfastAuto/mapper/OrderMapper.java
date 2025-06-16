package com.group2.VinfastAuto.mapper;

import com.group2.VinfastAuto.dto.request.OrderCreationRequest;
import com.group2.VinfastAuto.dto.request.OrderUpdateRequest;
import com.group2.VinfastAuto.dto.response.OrderResponse;
import com.group2.VinfastAuto.entity.Order;
import org.mapstruct.*;

/**
 * Mapper interface for converting between Order entities and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    // --- 1. Map từ Order -> OrderResponse ---
    @Mapping(target = "customerName", expression = "java(order.getUser() != null ? (order.getUser().getLastName() + ' ' + order.getUser().getFirstName()) : null)")
    @Mapping(target = "userId", expression = "java(order.getUser() != null ? order.getUser().getId() : null)")
    @Mapping(target = "carId", expression = "java(order.getCar() != null ? order.getCar().getCarId() : null)")
    @Mapping(target = "phoneNumber", source = "user.mobilephone")
    @Mapping(target = "carModel", source = "car.name")
    @Mapping(target = "carPrice", source = "car.price") // Get price from the car entity
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "address", source = "user.position")
    @Mapping(target = "orderDate", expression = "java(order.getOrderDate() != null ? order.getOrderDate().toLocalDate() : null)")
    OrderResponse orderToOrderResponse(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "orderDate", expression = "java(request.getOrderDate() != null ? request.getOrderDate().atStartOfDay() : null)")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    Order orderCreationRequestToOrder(OrderCreationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "orderDate", expression = "java(request.getOrderDate() != null ? request.getOrderDate().atStartOfDay() : order.getOrderDate())")
    void updateOrder(@MappingTarget Order order, OrderUpdateRequest request);
}
