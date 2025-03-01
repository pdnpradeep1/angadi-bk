package com.ecom.pradeep.angadi_bk.repo;

import com.ecom.pradeep.angadi_bk.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByStoreId(Long storeId);

    Page<Product> findByStoreId(Long storeId, Pageable pageable);

    List<Product> findByStoreIdAndStatus(Long storeId, String status);

    List<Product> findByCategoryId(Long categoryId);

    Optional<Product> findBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.stockQuantity <= p.lowStockThreshold")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND p.stockQuantity > 0 AND p.stockQuantity <= p.lowStockThreshold")
    List<Product> findLowStockProductsByStore(Long storeId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.store.id = :storeId")
    long countByStore(Long storeId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.store.id = :storeId AND p.status = :status")
    long countByStoreAndStatus(Long storeId, String status);

    List<Product> findByFeaturedTrue();

    List<Product> findByStoreIdAndFeaturedTrue(Long storeId);

    // Use @Query to define a custom query instead of relying on method name derivation
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND p.stockQuantity <= :upperLimit AND p.stockQuantity > :lowerLimit")
    List<Product> findByStoreIdAndStockQuantityBetween(
            @Param("storeId") Long storeId,
            @Param("upperLimit") int upperLimit,
            @Param("lowerLimit") int lowerLimit
    );

    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND p.stockQuantity <= :limit")
    List<Product> findByStoreIdAndStockQuantityLessThanOrEqualTo(
            @Param("storeId") Long storeId,
            @Param("limit") int limit
    );
}