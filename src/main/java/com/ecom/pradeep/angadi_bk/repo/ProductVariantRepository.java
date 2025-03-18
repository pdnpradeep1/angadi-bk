package com.ecom.pradeep.angadi_bk.repo;

import com.ecom.pradeep.angadi_bk.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * Find all variants for a specific product
     *
     * @param productId The product ID
     * @return List of product variants
     */
    List<ProductVariant> findByProductId(Long productId);

    /**
     * Delete all variants for a specific product
     *
     * @param productId The product ID
     */
    @Modifying
    @Query("DELETE FROM ProductVariant v WHERE v.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    /**
     * Find a variant by SKU
     *
     * @param sku The SKU to search for
     * @return The variant with the given SKU, if any
     */
    ProductVariant findBySku(String sku);

    /**
     * Check if a variant is in stock
     *
     * @param variantId The variant ID
     * @return true if in stock, false otherwise
     */
    @Query("SELECT CASE WHEN v.stockQuantity > 0 OR v.stockQuantity = -1 THEN true ELSE false END FROM ProductVariant v WHERE v.id = :variantId")
    boolean isVariantInStock(@Param("variantId") Long variantId);
}