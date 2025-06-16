package com.group2.VinfastAuto.repository;



import com.group2.VinfastAuto.entity.Order;
import com.group2.VinfastAuto.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"user", "car"})
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "car"})
    Page<Order> findByUser_LastNameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);    List<Order> findByStatus(OrderStatus status);

     @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countTotalOrdersByStatus();


}
