package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, String> {

    List<StockReservation> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    void deleteByOrderId(String orderId);
}
