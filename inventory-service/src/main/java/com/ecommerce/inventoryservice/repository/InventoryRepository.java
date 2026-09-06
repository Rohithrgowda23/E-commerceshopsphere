package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, String> {

    /**
     * Pessimistic write lock used specifically for reserve/release/reduce
     * operations, where we read-then-write availableQuantity and cannot
     * risk a lost update even under high concurrency (e.g. a flash sale).
     * Plain reads (GET /api/inventory/{id}) use the default findById.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByIdForUpdate(String productId);
}
