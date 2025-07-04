package com.group2.VinfastAuto.repository;

import com.group2.VinfastAuto.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
} 