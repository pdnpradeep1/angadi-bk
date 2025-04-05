package com.ecom.pradeep.angadi_bk.repo;

import com.ecom.pradeep.angadi_bk.model.ProductVariant;
import com.ecom.pradeep.angadi_bk.model.ProductVariantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, ProductVariantId> {

    /**
     * Find all variants for a specific product
     *
     * @param productId The product ID
     * @return List of product variants
     */
    @Query("SELECT v FROM ProductVariant v WHERE v.id.productId = :productId")
    List<ProductVariant> findByProductId(@Param("productId") Long productId);

    /**
     * Delete all variants for a specific product
     *
     * @param productId The product ID
     */
    @Modifying
    @Query("DELETE FROM ProductVariant v WHERE v.id.productId = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    /**
     * Find a variant by SKU
     *
     * @param sku The SKU to search for
     * @return The variant with the given SKU, if any
     */
    Optional<ProductVariant> findBySku(String sku);

    /**
     * Check if a variant is in stock
     *
     * @param variantId The variant ID component
     * @param productId The product ID component
     * @return true if in stock, false otherwise
     */
    @Query("SELECT CASE WHEN v.stockQuantity > 0 OR v.stockQuantity = -1 THEN true ELSE false END " +
            "FROM ProductVariant v WHERE v.id.variantId = :variantId AND v.id.productId = :productId")
    boolean isVariantInStock(@Param("variantId") Long variantId, @Param("productId") Long productId);

    /**
     * Find a variant by its composite ID components
     *
     * @param variantId The variant ID component
     * @param productId The product ID component
     * @return Optional containing the variant if found
     */
    @Query("SELECT v FROM ProductVariant v WHERE v.id.variantId = :variantId AND v.id.productId = :productId")
    Optional<ProductVariant> findByVariantIdAndProductId(
            @Param("variantId") Long variantId,
            @Param("productId") Long productId
    );

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = ?1 AND v.id = ?2")
    Optional<ProductVariant> findByProductIdAndVariantId(Long productId, Long variantId);

    @Query("SELECT v FROM ProductVariant v WHERE v.id.variantId = :variantId")
    List<ProductVariant> findByVariantId(@Param("variantId") Long variantId);

//     List<ProductVariant> findByProductId(Long productId);
}