package com.ecom.pradeep.angadi_bk.repo;

import com.ecom.pradeep.angadi_bk.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * Find all products for a store
     * @param storeId Store ID
     * @return List of products
     */
    List<Product> findByStoreId(Long storeId);

    /**
     * Find products for a store with pagination
     * @param storeId Store ID
     * @param pageable Pagination parameters
     * @return Page of products
     */
    Page<Product> findByStoreId(Long storeId, Pageable pageable);

    /**
     * Find products by store and status
     * @param storeId Store ID
     * @param status Product status
     * @return List of products
     */
    List<Product> findByStoreIdAndStatus(Long storeId, String status);

    /**
     * Find products by store and status with pagination
     * @param storeId Store ID
     * @param status Product status
     * @param pageable Pagination parameters
     * @return Page of products
     */
    Page<Product> findByStoreIdAndStatus(Long storeId, String status, Pageable pageable);

    /**
     * Find products by category
     * @param categoryId Category ID
     * @return List of products
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Find products by category with pagination
     * @param categoryId Category ID
     * @param pageable Pagination parameters
     * @return Page of products
     */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Find a product by SKU
     * @param sku Product SKU
     * @return Optional containing the product if found
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find low stock products
     * @return List of products with stock below threshold
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.stockQuantity <= p.lowStockThreshold")
    List<Product> findLowStockProducts();

    /**
     * Find low stock products with pagination
     * @param pageable Pagination parameters
     * @return Page of products with stock below threshold
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.stockQuantity <= p.lowStockThreshold")
    Page<Product> findLowStockProducts(Pageable pageable);

    /**
     * Find low stock products for a specific store
     * @param storeId Store ID
     * @return List of products with stock below threshold
     */
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND p.stockQuantity > 0 AND p.stockQuantity <= p.lowStockThreshold")
    List<Product> findLowStockProductsByStore(Long storeId);

    /**
     * Find low stock products for a specific store with pagination
     * @param storeId Store ID
     * @param pageable Pagination parameters
     * @return Page of products with stock below threshold
     */
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND p.stockQuantity > 0 AND p.stockQuantity <= p.lowStockThreshold")
    Page<Product> findLowStockProductsByStore(@Param("storeId") Long storeId, Pageable pageable);

    /**
     * Count products by store
     * @param storeId Store ID
     * @return Product count
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.store.id = :storeId")
    long countByStore(Long storeId);

    /**
     * Count products by store and status
     * @param storeId Store ID
     * @param status Product status
     * @return Product count
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.store.id = :storeId AND p.status = :status")
    long countByStoreAndStatus(Long storeId, String status);

    /**
     * Find featured products
     * @return List of featured products
     */
    List<Product> findByFeaturedTrue();

    /**
     * Find featured products with pagination
     * @param pageable Pagination parameters
     * @return Page of featured products
     */
    Page<Product> findByFeaturedTrue(Pageable pageable);

    /**
     * Find featured products for a specific store
     * @param storeId Store ID
     * @return List of featured products
     */
    List<Product> findByStoreIdAndFeaturedTrue(Long storeId);

    /**
     * Find featured products for a specific store with pagination
     * @param storeId Store ID
     * @param pageable Pagination parameters
     * @return Page of featured products
     */
    Page<Product> findByStoreIdAndFeaturedTrue(Long storeId, Pageable pageable);

    /**
     * Find products within a stock quantity range
     * @param storeId Store ID
     * @param upperLimit Upper limit of stock quantity
     * @param lowerLimit Lower limit of stock quantity
     * @return List of products
     */
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND p.stockQuantity <= :upperLimit AND p.stockQuantity > :lowerLimit")
    List<Product> findByStoreIdAndStockQuantityBetween(
            @Param("storeId") Long storeId,
            @Param("upperLimit") int upperLimit,
            @Param("lowerLimit") int lowerLimit
    );

    /**
     * Find products within a stock quantity range with pagination
     * @param storeId Store ID
     * @param upperLimit Upper limit of stock quantity
     * @param lowerLimit Lower limit of stock quantity
     * @param pageable Pagination parameters
     * @return Page of products
     */
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND p.stockQuantity <= :upperLimit AND p.stockQuantity > :lowerLimit")
    Page<Product> findByStoreIdAndStockQuantityBetween(
            @Param("storeId") Long storeId,
            @Param("upperLimit") int upperLimit,
            @Param("lowerLimit") int lowerLimit,
            Pageable pageable
    );

    /**
     * Find products with stock quantity less than or equal to a limit
     * @param storeId Store ID
     * @param limit Stock quantity limit
     * @return List of products
     */
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND p.stockQuantity <= :limit")
    List<Product> findByStoreIdAndStockQuantityLessThanOrEqualTo(
            @Param("storeId") Long storeId,
            @Param("limit") int limit
    );

    /**
     * Find products with stock quantity less than or equal to a limit with pagination
     * @param storeId Store ID
     * @param limit Stock quantity limit
     * @param pageable Pagination parameters
     * @return Page of products
     */
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND p.stockQuantity <= :limit")
    Page<Product> findByStoreIdAndStockQuantityLessThanOrEqualTo(
            @Param("storeId") Long storeId,
            @Param("limit") int limit,
            Pageable pageable
    );

    /**
     * Find a product by ID and store ID
     * @param productId Product ID
     * @param storeId Store ID
     * @return Optional containing the product if found
     */
    Optional<Product> findByIdAndStoreId(Long productId, Long storeId);

    /**
     * Count products by category
     * @param categoryId Category ID
     * @return Product count
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Search products by keyword in name or description with pagination
     * @param storeId Store ID
     * @param keyword Search keyword
     * @param pageable Pagination parameters
     * @return Page of matching products
     */
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("storeId") Long storeId, @Param("keyword") String keyword, Pageable pageable);
}