package com.group2.VinfastAuto.mapper;

import com.group2.VinfastAuto.dto.request.OrderCreationRequest;
import com.group2.VinfastAuto.dto.request.OrderUpdateRequest;
import com.group2.VinfastAuto.dto.response.OrderResponse;
import com.group2.VinfastAuto.entity.Order;
import org.mapstruct.*;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    // --- 1. Map từ Order -> OrderResponse ---
    @Mapping(target = "customerName", expression = "java(order.getUser() != null ? (order.getUser().getLastName() + ' ' + order.getUser().getFirstName()) : null)")
    @Mapping(target = "phoneNumber", source = "user.mobilephone")
    @Mapping(target = "carModel", source = "car.name")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "address", source = "user.position")
    OrderResponse orderToOrderResponse(Order order);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "car", ignore = true)
    Order orderCreationRequestToOrder(OrderCreationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "car", ignore = true)
    void updateOrder(@MappingTarget Order order, OrderUpdateRequest request);
}
