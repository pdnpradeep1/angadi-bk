package com.ecom.pradeep.angadi_bk.repo;

import com.ecom.pradeep.angadi_bk.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Page<Order> findByStoreId(Long storeId, Pageable pageable);

    Page<Order> findByStoreIdAndStatus(Long storeId, String status, Pageable pageable);

    List<Order> findByStatus(String status);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId")
    List<Order> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.customer.id = :customerId AND o.product.id = :productId")
    boolean existsByCustomerIdAndProductId(@Param("customerId") Long customerId, @Param("productId") Long productId);

    @Query("SELECT o FROM Order o WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumber(@Param("orderNumber") String orderNumber);

    @Query("SELECT o FROM Order o WHERE o.store.id = :storeId AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByStoreIdAndCreatedAtBetween(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.store.id = :storeId AND o.customer.email LIKE %:email%")
    Page<Order> findByStoreIdAndCustomerEmailContaining(
            @Param("storeId") Long storeId,
            @Param("email") String email,
            Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.store.id = :storeId AND o.totalAmount BETWEEN :minAmount AND :maxAmount")
    Page<Order> findByStoreIdAndTotalAmountBetween(
            @Param("storeId") Long storeId,
            @Param("minAmount") Double minAmount,
            @Param("maxAmount") Double maxAmount,
            Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.store.id = :storeId")
    Long countByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.store.id = :storeId AND o.status = :status")
    Long countByStoreIdAndStatus(@Param("storeId") Long storeId, @Param("status") String status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.store.id = :storeId AND o.status != 'CANCELLED'")
    Double sumTotalAmountByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.store.id = :storeId AND o.createdAt BETWEEN :startDate AND :endDate AND o.status != 'CANCELLED'")
    Double sumTotalAmountByStoreIdAndCreatedAtBetween(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Search order by keywords in orderNumber, customer name, or customer email
    @Query("SELECT o FROM Order o WHERE o.store.id = :storeId " +
            "AND (o.orderNumber LIKE %:keyword% " +
            "OR o.customer.name LIKE %:keyword% " +
            "OR o.customer.email LIKE %:keyword%)")
    Page<Order> searchByKeyword(
            @Param("storeId") Long storeId,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.store.id = :storeId AND FUNCTION('DATE', o.createdAt) = CURRENT_DATE")
    Long countTodayOrdersByStoreId(@Param("storeId") Long storeId);
}